package com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase

import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.CANDIDATE_EXTENSIONS
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.initExtensionCache
import com.minhtu.firesocialmedia.android.service.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.platform.AppConfig
import com.minhtu.firesocialmedia.platform.logMessage
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URLDecoder

class SupabaseStorageHelper {
    fun getFileExtension(path: String, defaultExt: String): String {
        // For content:// URIs, sniff MIME type from ContentResolver
        if (path.startsWith("content://")) {
            val context = com.minhtu.firesocialmedia.platform.getAppContext()
            val mime = context.contentResolver.getType(android.net.Uri.parse(path))
            if (mime != null) {
                return when (mime.lowercase()) {
                    "image/jpeg" -> "jpg"
                    "image/png"  -> "png"
                    "image/gif"  -> "gif"
                    "image/webp" -> "webp"
                    "video/mp4"  -> "mp4"
                    "video/quicktime", "video/mov" -> "mov"
                    else -> defaultExt
                }
            }
            return defaultExt
        }
        val ext = path.substringAfterLast('.', "").lowercase()
        return if (ext.length in 2..5) ext else defaultExt
    }

    suspend fun deleteOldMediaIfNeeded(path: String) {
        if (path.isEmpty()) return

        val cleanPath = extractRelativePath(path) ?: return

        runCatching {
            try{
                deleteFromSupabaseStorage(cleanPath)
            } catch (e : Exception) {
                logMessage("deleteOldMediaIfNeeded", { "Error happened: ${e.message}" })
            }
        }
    }

    fun extractRelativePath(input: String): String? {
        return when {
            input.contains("supabase.co") ->
                input.substringAfter("/object/public/uploads/")

            input.contains("firebasestorage.googleapis.com") -> {
                val encoded = input.substringAfter("/o/").substringBefore("?")
                URLDecoder.decode(encoded, "UTF-8")
            }

            input.contains("/") -> input // already relative
            else -> null
        }
    }

    companion object {
        private const val BASE_BUCKET_ENDPOINT = "storage/v1/object/public/uploads/"
        private const val SUPABASE_BASE = SupabaseClient.BASE_URL + BASE_BUCKET_ENDPOINT
        private const val PREFS_NAME = "supabase_ext_cache"
        private const val BUCKET = "uploads"

        /**
         * Delete the object at [remotePath] from Supabase Storage. Inlined here (rather than
         * delegating to a per-feature `SupabaseStorage` clone) because this helper itself is
         * shared, stateful infra that stays in core — see the note in KoinModules.kt.
         */
        private suspend fun deleteFromSupabaseStorage(remotePath: String) {
            val url = "${SupabaseClient.BASE_URL}storage/v1/object/$BUCKET/$remotePath"
            val response = KtorProvider.client.delete(url) {
                header("Authorization", "Bearer ${AppConfig.supabaseApiKey}")
            }
            if (!response.status.isSuccess()) {
                throw Exception("Delete failed: ${response.status.value} ${response.status.description}")
            }
        }

        /** Candidate extensions — images first (most common), then video. */
        private val CANDIDATE_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp", "gif", "mp4", "mov")

        /**
         * In-memory cache: extensionless base URL → resolved URL with extension.
         * Pre-populated from SharedPreferences on first call to [initExtensionCache].
         */
        private val extensionCache = java.util.concurrent.ConcurrentHashMap<String, String>()

        @Volatile private var sharedPrefs: android.content.SharedPreferences? = null

        /**
         * Call once at app startup (e.g. in [Application.onCreate]) to warm the
         * in-memory cache from the persisted SharedPreferences store.
         */
        fun initExtensionCache(context: android.content.Context) {
            val prefs = context.applicationContext
                .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            sharedPrefs = prefs
            prefs.all.forEach { (k, v) ->
                if (v is String) extensionCache[k] = v
            }
        }

        private fun persistEntry(key: String, value: String) {
            sharedPrefs?.edit()?.putString(key, value)?.apply()
        }

        /** Synchronous URL rewrite: Firebase URL → Supabase base URL (no network call). */
        fun resolveMediaUrl(input: String): String {
            if (input.isEmpty()) return input
            return when {
                input.contains("supabase.co") -> input
                input.contains("firebasestorage.googleapis.com") -> {
                    val path = try {
                        val encodedPath = input.substringAfter("/o/").substringBefore("?")
                        URLDecoder.decode(encodedPath, "UTF-8")
                    } catch (e: Exception) { return input }
                    SUPABASE_BASE + path
                }
                else -> SUPABASE_BASE + input
            }
        }

        /**
         * Async URL resolver that discovers the real file extension for extension-less
         * Supabase paths (legacy Firebase avatars stored under bare UIDs).
         *
         * Performance characteristics:
         * - Already-cached URLs: O(1) ConcurrentHashMap lookup, no I/O.
         * - Extension-bearing URLs: immediate return, no I/O.
         * - Unknown URLs: fires all [CANDIDATE_EXTENSIONS] HEAD requests in parallel
         *   on [Dispatchers.IO] and resolves as soon as the first 2xx reply arrives
         *   (remaining requests are cancelled). Result persisted to SharedPreferences
         *   so subsequent cold starts skip all HEAD calls.
         */
        suspend fun resolveMediaUrlAsync(input: String): String = withContext(Dispatchers.IO) {
            val base = resolveMediaUrl(input)
            if (base.isEmpty()) return@withContext base

            val cleanBase = base
                .substringBefore('?')
                .replace("/render/image/public/", "/object/public/")

            // Fast path 1: already has extension
            if (cleanBase.substringAfterLast('/').contains('.')) return@withContext cleanBase

            // Fast path 2: in-memory cache hit
            extensionCache[cleanBase]?.let { return@withContext it }

            // Slow path: fire all HEAD requests in parallel, take the first 2xx
            val result = coroutineScope {
                val channel = Channel<String>(capacity = 1)
                val jobs = CANDIDATE_EXTENSIONS.map { ext ->
                    async(Dispatchers.IO) {
                        val candidate = "$cleanBase.$ext"
                        if (headRequestSucceeds(candidate)) {
                            channel.trySend(candidate)
                        }
                    }
                }
                // Wait for first success or all completions
                val winner = withTimeoutOrNull(5_000) {
                    val deferred = async { channel.receiveCatching().getOrNull() }
                    jobs.forEach { it.join() }
                    channel.close()
                    deferred.await()
                }
                jobs.forEach { it.cancel() }
                winner
            }

            if (result != null) {
                extensionCache[cleanBase] = result
                persistEntry(cleanBase, result)
                result
            } else {
                // No extension found — cache the miss too so we don't retry
                extensionCache[cleanBase] = cleanBase
                cleanBase
            }
        }

        private fun headRequestSucceeds(url: String): Boolean {
            return try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 3_000
                connection.readTimeout = 3_000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "SocialMediaApp")
                val code = connection.responseCode
                connection.disconnect()
                code in 200..299
            } catch (_: Exception) { false }
        }
    }

}