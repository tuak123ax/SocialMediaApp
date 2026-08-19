package com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase

import com.minhtu.firesocialmedia.ios.service.serviceimpl.notification.KtorProvider
import io.ktor.client.request.head
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSUserDefaults
import platform.Foundation.create

class SupabaseStorageHelper {

    companion object {
        private const val BASE_BUCKET_ENDPOINT = "storage/v1/object/public/uploads/"
        private const val SUPABASE_BASE = SupabaseClient.BASE_URL + BASE_BUCKET_ENDPOINT

        /**
         * Resolve any stored path/URL (relative, Supabase, or old Firebase) to a full public URL.
         */
        fun resolveMediaUrl(input: String): String {
            if (input.isEmpty()) return input
            return when {
                input.contains("supabase.co") -> input
                input.contains("firebasestorage.googleapis.com") -> {
                    val path = try {
                        val encodedPath = input.substringAfter("/o/").substringBefore("?")
                        encodedPath
                    } catch (e: Exception) {
                        return input
                    }
                    SUPABASE_BASE + path
                }
                else -> SUPABASE_BASE + input
            }
        }

        /**
         * Extract the Supabase-relative path from any URL/path representation.
         * Returns null for old Firebase URLs (those objects don't exist in Supabase).
         */
        fun extractRelativePath(input: String): String? {
            return when {
                input.contains("supabase.co") ->
                    input.substringAfter("/object/public/uploads/")
                input.contains("firebasestorage.googleapis.com") ->
                    null // skip; object doesn't exist in Supabase
                input.contains("/") -> input // already a relative path
                else -> null
            }
        }

        // ─── Extension-check cache ────────────────────────────────────────────

        private val extensionCache = HashMap<String, String>()
        private const val PREFS_KEY = "supabase_ext_cache_v1"
        private val CANDIDATE_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp", "gif", "mp4", "mov")
        private val KNOWN_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "mp4", "mov")

        /** Call once at app startup to warm the cache from NSUserDefaults. */
        fun initExtensionCache() {
            val raw = NSUserDefaults.standardUserDefaults.stringForKey(PREFS_KEY) ?: return
            raw.split("|").forEach { entry ->
                val idx = entry.indexOf('=')
                if (idx > 0) {
                    val k = entry.substring(0, idx).cacheUnescape()
                    val v = entry.substring(idx + 1).cacheUnescape()
                    extensionCache[k] = v
                }
            }
        }

        private fun persistEntry(key: String, value: String) {
            val prefs = NSUserDefaults.standardUserDefaults
            val existing = prefs.stringForKey(PREFS_KEY) ?: ""
            val newEntry = "${key.cacheEscape()}=${value.cacheEscape()}"
            val updated = if (existing.isEmpty()) newEntry else "$existing|$newEntry"
            prefs.setObject(NSString.create(string = updated), forKey = PREFS_KEY)
        }

        private fun String.cacheEscape() = replace("%", "%25").replace("|", "%7C").replace("=", "%3D")
        private fun String.cacheUnescape() = replace("%3D", "=").replace("%7C", "|").replace("%25", "%")

        /**
         * Async version of resolveMediaUrl that fires parallel HEAD requests
         * to discover the real file extension for legacy extensionless paths.
         * Result is cached in memory and persisted to NSUserDefaults.
         */
        suspend fun resolveMediaUrlAsync(input: String): String = withContext(Dispatchers.Default) {
            if (input.isEmpty()) return@withContext input
            // Fast path 1: input already has a known extension
            val ext = input.substringAfterLast('.', "").lowercase()
            if (ext in KNOWN_EXTENSIONS) return@withContext resolveMediaUrl(input)
            // Build the base Supabase URL
            val baseUrl = resolveMediaUrl(input)
            // Fast path 2: base URL itself has a known extension
            val baseExt = baseUrl.substringAfterLast('.', "").lowercase()
            if (baseExt in KNOWN_EXTENSIONS) return@withContext baseUrl
            // Fast path 3: memory/persistent cache hit
            extensionCache[baseUrl]?.let { return@withContext "$baseUrl.$it" }
            // Slow path: try all candidate extensions in parallel via HEAD
            val winner = coroutineScope {
                CANDIDATE_EXTENSIONS.map { candidate ->
                    async { if (headRequestSucceeds("$baseUrl.$candidate")) candidate else null }
                }.awaitAll().firstOrNull { it != null }
            }
            if (winner != null) {
                extensionCache[baseUrl] = winner
                persistEntry(baseUrl, winner)
                "$baseUrl.$winner"
            } else {
                baseUrl
            }
        }

        private suspend fun headRequestSucceeds(url: String): Boolean = runCatching {
            KtorProvider.client.head(url).status.isSuccess()
        }.getOrDefault(false)
    }
}