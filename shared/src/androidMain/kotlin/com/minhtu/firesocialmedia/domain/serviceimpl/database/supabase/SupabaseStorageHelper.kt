package com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.serviceimpl.database.StorageHelperInterface
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URLDecoder

class SupabaseStorageHelper : StorageHelperInterface {
    override suspend fun saveInstanceToDatabase(
        commentId: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean {
        Log.d("Task", "saveInstanceToDatabase")

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference()
            .child(path)
            .child(commentId)

        return try {
            when {
                instance.image.isNotEmpty() -> {
                    val extension = getFileExtension(instance.image, "jpg")
                    val remotePath = "$path/${commentId}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = instance.image,
                        remotePath = remotePath
                    )

                    instance.updateImage(remotePath) // store relative path
                    instance.updateVideo("")
                }

                instance.video.isNotEmpty() -> {
                    val extension = getFileExtension(instance.video, "mp4")
                    val remotePath = "$path/${commentId}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = instance.video,
                        remotePath = remotePath
                    )

                    instance.updateVideo(remotePath) // store relative path
                    instance.updateImage("")
                }

                else -> {
                    // no media
                }
            }

            databaseReference.setValue(instance).await()
            true

        } catch (e: Exception) {
            logMessage(
                "saveInstanceToDatabase",
                { "Exception with Supabase upload: ${e.message}" }
            )
            false
        }
    }

    override suspend fun deleteNewsFromDatabase(
        path: String,
        new: NewsDTO
    ) {
        Log.d("Task", "deleteNewsFromDatabase")

        // 1. Delete from Firebase Realtime DB
        FirebaseDatabase.getInstance()
            .getReference()
            .child(path)
            .child(new.id)
            .removeValue()
            .await()

        // 2. Delete media from Supabase Storage
        try {
            when {
                new.image.isNotEmpty() -> {
                    SupabaseStorage.delete(new.image) // correct path
                }

                new.video.isNotEmpty() -> {
                    SupabaseStorage.delete(new.video) // correct path
                }
            }
        } catch (e: Exception) {
            Log.w("Task", "Supabase Storage delete: ${e.message}")
        }
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean {
        Log.d("Task", "updateNewsFromDatabase")

        val dbRef = FirebaseDatabase.getInstance()
            .getReference(path)
            .child(new.id)

        return try {
            val updates = mutableMapOf<String, Any>(
                "message" to newContent
            )

            when {
                // Image branch
                newImage.isNotEmpty() -> {
                    if (newImage != new.image) {

                        val extension = getFileExtension(newImage, "jpg")
                        val remotePath =
                            "$path/${new.id}_${System.currentTimeMillis()}.$extension"

                        SupabaseStorage.upload(
                            filePath = newImage,
                            remotePath = remotePath
                        )

                        // delete old file safely
                        deleteOldMediaIfNeeded(new.image)

                        updates["image"] = remotePath
                        updates["video"] = ""
                    } else {
                        updates["image"] = new.image
                        updates["video"] = ""
                    }
                }

                // Video branch
                newVideo.isNotEmpty() -> {
                    if (newVideo != new.video) {

                        val extension = getFileExtension(newVideo, "mp4")
                        val remotePath =
                            "$path/${new.id}_${System.currentTimeMillis()}.$extension"

                        SupabaseStorage.upload(
                            filePath = newVideo,
                            remotePath = remotePath
                        )

                        // delete old file safely
                        deleteOldMediaIfNeeded(new.video)

                        updates["video"] = remotePath
                        updates["image"] = ""
                    } else {
                        updates["video"] = new.video
                        updates["image"] = ""
                    }
                }

                // No media → clear & delete old file
                else -> {
                    updates["image"] = ""
                    updates["video"] = ""

                    deleteOldMediaIfNeeded(new.image)
                    deleteOldMediaIfNeeded(new.video)
                }
            }

            dbRef.updateChildren(updates).await()
            true

        } catch (t: Throwable) {
            Log.e("Task", "updateNewsFromDatabase failed", t)
            false
        }
    }

    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean {
        return runCatching {
            val dbRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(path)
                .child(commentId)

            when {
                instance.image.isNotEmpty() -> {
                    val filePath = instance.localPath.ifEmpty { instance.image }
                    val extension = getFileExtension(filePath, "jpg")

                    val remotePath =
                        "$path/${commentId}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    instance.updateImage(remotePath) // store path only
                    instance.updateVideo("")
                }

                instance.video.isNotEmpty() -> {
                    val filePath = instance.localPath.ifEmpty { instance.video }
                    val extension = getFileExtension(filePath, "mp4")

                    val remotePath =
                        "$path/${commentId}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    instance.updateVideo(remotePath) // store path only
                    instance.updateImage("")
                }

                else -> {
                    // No media → just save
                }
            }

            dbRef.setValue(instance).await()
            true

        }.getOrElse { e ->
            Log.e("Task", "saveNewToDatabase failed: ${e.message}", e)
            false
        }
    }

    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        val databaseRef = FirebaseDatabase.getInstance().reference

        return try {
            val shouldUploadAvatar =
                group.avatar != Constants.DEFAULT_AVATAR_URL &&
                        group.avatar != Constants.DEFAULT_DECADE_AVATAR_URL &&
                        group.avatar != Constants.DEFAULT_ARK_AVATAR_URL_FOR_GROUP

            if (shouldUploadAvatar) {
                val extension = getFileExtension(group.avatar, "jpg")

                val remotePath =
                    "$groupRootPath/$groupAvatarsStoragePath/${group.id}_${System.currentTimeMillis()}.$extension"

                SupabaseStorage.upload(
                    filePath = group.avatar,
                    remotePath = remotePath
                )

                group.avatar = remotePath // store relative path
            }

            updateGroupDataOnServer(
                databaseRef = databaseRef,
                groupRootPath = groupRootPath,
                userRootPath = userRootPath,
                userGroupsField = userGroupsField,
                group = group,
                userId = userId
            )

            true

        } catch (ex: Exception) {
            logMessage(
                "saveGroupAndUserGroups",
                { "Exception with Supabase: ${ex.message}" }
            )
            false
        }
    }

    suspend fun updateGroupDataOnServer(
        databaseRef: DatabaseReference,
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        return try {
            val groupSummary = GroupSummaryDTO(
                id = group.id,
                name = group.name,
                avatar = group.avatar
            )

            val updates = hashMapOf<String, Any?>(
                "$groupRootPath/${group.id}" to group,
                "$userRootPath/$userId/$userGroupsField/${group.id}" to groupSummary
            )

            databaseRef.updateChildren(updates).await()

            Log.d("Task", "updateChildren SUCCESS")
            true

        } catch (e: Exception) {
            Log.e("Task", "updateChildren FAILED", e)
            false
        }
    }

    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean {
        return runCatching {
            val dbRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(groupPath)
                .child(groupId)
                .child(postsPath)
                .child(newsDTO.id)

            when {
                newsDTO.image.isNotEmpty() -> {
                    val filePath = newsDTO.localPath.ifEmpty { newsDTO.image }
                    val extension = getFileExtension(filePath, "jpg")

                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    newsDTO.updateImage(remotePath) // store relative path
                    newsDTO.updateVideo("")
                }

                newsDTO.video.isNotEmpty() -> {
                    val filePath = newsDTO.localPath.ifEmpty { newsDTO.video }
                    val extension = getFileExtension(filePath, "mp4")

                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    newsDTO.updateVideo(remotePath) // store relative path
                    newsDTO.updateImage("")
                }

                else -> {
                    // No media → just save
                }
            }

            dbRef.setValue(newsDTO).await()
            true

        }.getOrElse { e ->
            Log.e("Task", "saveNewToGroup failed: ${e.message}", e)
            false
        }
    }

    override suspend fun saveSignUpInformation(user: UserDTO): Boolean {
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference()
            .child("users")
            .child(user.uid)

        return try {
            val shouldUploadAvatar =
                user.image != Constants.DEFAULT_AVATAR_URL &&
                        user.image != Constants.DEFAULT_DECADE_AVATAR_URL &&
                        user.image != Constants.DEFAULT_ARK_AVATAR_URL_FOR_GROUP

            if (shouldUploadAvatar) {
                val extension = getFileExtension(user.image, "jpg")

                val remotePath =
                    "avatar/${user.uid}_${System.currentTimeMillis()}.$extension"

                SupabaseStorage.upload(
                    filePath = user.image,
                    remotePath = remotePath
                )

                user.updateImage(remotePath) // store relative path
            }

            databaseReference.setValue(user).await()
            true

        } catch (e: Exception) {
            Log.e("Task", "saveSignUpInformation failed: ${e.message}", e)
            false
        }
    }

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
                SupabaseStorage.delete(cleanPath)
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
                    // Drain jobs; return first channel value
                    val deferred = async { channel.receive() }
                    jobs.forEach { it.join() }
                    channel.close()
                    runCatching { deferred.await() }.getOrNull()
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