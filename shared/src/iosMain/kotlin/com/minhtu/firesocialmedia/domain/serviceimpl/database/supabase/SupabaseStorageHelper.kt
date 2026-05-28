package com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase

import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.toMap
import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.serviceimpl.database.StorageHelperInterface
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithDouble
import platform.Foundation.numberWithFloat
import platform.Foundation.numberWithInt
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class SupabaseStorageHelper : StorageHelperInterface {

    private val database: FIRDatabaseReference
        get() = FIRDatabase.database().reference()

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean {
        logMessage("SupabaseStorageHelper", { "saveInstanceToDatabase" })
        val dbRef = database.child(path).child(id)
        return try {
            when {
                instance.image.isNotEmpty() -> {
                    val bytes = Base64.Default.decode(instance.image)
                    val remotePath = "$path/${id}_${currentTimeMillis()}.jpg"
                    SupabaseStorage.uploadBytes(bytes, remotePath)
                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }
                instance.video.isNotEmpty() -> {
                    // video is a file URI string on iOS
                    val remotePath = "$path/${id}_${currentTimeMillis()}.mp4"
                    SupabaseStorage.uploadFile(instance.video, remotePath)
                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }
                else -> { /* no media */ }
            }
            setValueSuspend(dbRef, instance.toMap())
            true
        } catch (e: Exception) {
            logMessage("SupabaseStorageHelper", { "saveInstanceToDatabase failed: ${e.message}" })
            false
        }
    }

    override suspend fun deleteNewsFromDatabase(path: String, new: NewsDTO) {
        logMessage("SupabaseStorageHelper", { "deleteNewsFromDatabase" })
        try {
            val dbRef = database.child(path).child(new.id)
            removeValueSuspend(dbRef)
        } catch (e: Exception) {
            logMessage("SupabaseStorageHelper", { "DB delete failed: ${e.message}" })
        }
        try {
            when {
                new.image.isNotEmpty() -> SupabaseStorage.delete(new.image)
                new.video.isNotEmpty() -> SupabaseStorage.delete(new.video)
            }
        } catch (e: Exception) {
            logMessage("SupabaseStorageHelper", { "Storage delete failed: ${e.message}" })
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean {
        logMessage("SupabaseStorageHelper", { "updateNewsFromDatabase" })
        val dbRef = database.child(path).child(new.id)
        return try {
            val updates = mutableMapOf<String, Any>("message" to newContent)
            when {
                newImage.isNotEmpty() -> {
                    if (newImage != new.image) {
                        val bytes = Base64.Default.decode(newImage)
                        val remotePath = "$path/${new.id}_${currentTimeMillis()}.jpg"
                        SupabaseStorage.uploadBytes(bytes, remotePath)
                        deleteOldMediaIfNeeded(new.image)
                        updates["image"] = remotePath
                        updates["video"] = ""
                    } else {
                        updates["image"] = new.image
                        updates["video"] = ""
                    }
                }
                newVideo.isNotEmpty() -> {
                    if (newVideo != new.video) {
                        val remotePath = "$path/${new.id}_${currentTimeMillis()}.mp4"
                        SupabaseStorage.uploadFile(newVideo, remotePath)
                        deleteOldMediaIfNeeded(new.video)
                        updates["video"] = remotePath
                        updates["image"] = ""
                    } else {
                        updates["video"] = new.video
                        updates["image"] = ""
                    }
                }
                else -> {
                    updates["image"] = ""
                    updates["video"] = ""
                    deleteOldMediaIfNeeded(new.image)
                    deleteOldMediaIfNeeded(new.video)
                }
            }
            updateChildrenSuspend(dbRef, updates)
            true
        } catch (t: Throwable) {
            logMessage("SupabaseStorageHelper", { "updateNewsFromDatabase failed: ${t.message}" })
            false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean {
        return runCatching {
            val dbRef = database.child(path).child(commentId)
            when {
                instance.image.isNotEmpty() -> {
                    val sourceBase64 = instance.localPath.ifEmpty { instance.image }
                    val bytes = Base64.Default.decode(sourceBase64)
                    val remotePath = "$path/${commentId}_${currentTimeMillis()}.jpg"
                    SupabaseStorage.uploadBytes(bytes, remotePath)
                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }
                instance.video.isNotEmpty() -> {
                    val sourcePath = instance.localPath.ifEmpty { instance.video }
                    val remotePath = "$path/${commentId}_${currentTimeMillis()}.mp4"
                    SupabaseStorage.uploadFile(sourcePath, remotePath)
                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }
                else -> { /* no media */ }
            }
            setValueSuspend(dbRef, instance.toMap())
            true
        }.getOrElse { e ->
            logMessage("SupabaseStorageHelper", { "saveNewToDatabase failed: ${e.message}" })
            false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        return try {
            val shouldUploadAvatar =
                group.avatar != Constants.DEFAULT_AVATAR_URL &&
                group.avatar != Constants.DEFAULT_DECADE_AVATAR_URL &&
                group.avatar != Constants.DEFAULT_ARK_AVATAR_URL_FOR_GROUP

            if (shouldUploadAvatar) {
                val bytes = Base64.Default.decode(group.avatar)
                val remotePath =
                    "$groupRootPath/$groupAvatarsStoragePath/${group.id}_${currentTimeMillis()}.jpg"
                SupabaseStorage.uploadBytes(bytes, remotePath)
                group.avatar = remotePath
            }
            updateGroupDataOnServer(
                dbRoot = database,
                groupRootPath = groupRootPath,
                userRootPath = userRootPath,
                userGroupsField = userGroupsField,
                group = group,
                userId = userId
            )
            true
        } catch (e: Exception) {
            logMessage("SupabaseStorageHelper", { "saveGroupAndUserGroups failed: ${e.message}" })
            false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean {
        return runCatching {
            val dbRef = database.child(groupPath).child(groupId).child(postsPath).child(newsDTO.id)
            when {
                newsDTO.image.isNotEmpty() -> {
                    val sourceBase64 = newsDTO.localPath.ifEmpty { newsDTO.image }
                    val bytes = Base64.Default.decode(sourceBase64)
                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${currentTimeMillis()}.jpg"
                    SupabaseStorage.uploadBytes(bytes, remotePath)
                    newsDTO.updateImage(remotePath)
                    newsDTO.updateVideo("")
                }
                newsDTO.video.isNotEmpty() -> {
                    val sourcePath = newsDTO.localPath.ifEmpty { newsDTO.video }
                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${currentTimeMillis()}.mp4"
                    SupabaseStorage.uploadFile(sourcePath, remotePath)
                    newsDTO.updateVideo(remotePath)
                    newsDTO.updateImage("")
                }
                else -> { /* no media */ }
            }
            setValueSuspend(dbRef, newsDTO.toMap())
            true
        }.getOrElse { e ->
            logMessage("SupabaseStorageHelper", { "saveNewToGroup failed: ${e.message}" })
            false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveSignUpInformation(user: UserDTO): Boolean {
        val dbRef = database.child("users").child(user.uid)
        return try {
            val shouldUploadAvatar =
                user.image != Constants.DEFAULT_AVATAR_URL &&
                user.image != Constants.DEFAULT_DECADE_AVATAR_URL &&
                user.image != Constants.DEFAULT_ARK_AVATAR_URL_FOR_GROUP

            if (shouldUploadAvatar) {
                val bytes = Base64.Default.decode(user.image)
                val remotePath = "avatar/${user.uid}_${currentTimeMillis()}.jpg"
                SupabaseStorage.uploadBytes(bytes, remotePath)
                user.updateImage(remotePath)
            }
            setValueSuspend(dbRef, user.toMap())
            true
        } catch (e: Exception) {
            logMessage("SupabaseStorageHelper", { "saveSignUpInformation failed: ${e.message}" })
            false
        }
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private fun currentTimeMillis(): Long =
        (platform.Foundation.NSDate.date().timeIntervalSince1970 * 1000).toLong()

    private suspend fun deleteOldMediaIfNeeded(path: String) {
        if (path.isEmpty()) return
        val cleanPath = extractRelativePath(path) ?: return
        try {
            SupabaseStorage.delete(cleanPath)
        } catch (e: Exception) {
            logMessage("SupabaseStorageHelper", { "deleteOldMediaIfNeeded error: ${e.message}" })
        }
    }

    private suspend fun setValueSuspend(ref: FIRDatabaseReference, value: Map<String, Any?>) =
        suspendCancellableCoroutine<Unit> { cont ->
            ref.setValue(prepareForFirebase(value)) { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }

    private suspend fun removeValueSuspend(ref: FIRDatabaseReference) =
        suspendCancellableCoroutine<Unit> { cont ->
            ref.removeValueWithCompletionBlock { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }

    private suspend fun updateChildrenSuspend(
        ref: FIRDatabaseReference,
        updates: Map<String, Any>
    ) = suspendCancellableCoroutine<Unit> { cont ->
        val castedUpdates: Map<Any?, *> = updates.entries.associate { it.key as Any? to it.value }
        ref.updateChildValues(castedUpdates) { error, _ ->
            if (error == null) cont.resume(Unit)
            else cont.resumeWithException(Throwable(error.localizedDescription))
        }
    }

    private suspend fun updateGroupDataOnServer(
        dbRoot: FIRDatabaseReference,
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        group: GroupDTO,
        userId: String
    ) {
        val groupSummary = GroupSummaryDTO(id = group.id, name = group.name, avatar = group.avatar)

        // Build GroupDTO map manually (Firebase needs NSDictionary, not Kotlin data class)
        val groupMap: Map<String, Any?> = mapOf(
            "id" to group.id,
            "name" to group.name,
            "avatar" to group.avatar,
            "password" to group.password,
            "description" to group.description,
            "createdDate" to group.createdDate,
            "memberCount" to group.memberCount,
            "members" to group.members
        )
        // Build GroupSummaryDTO map manually
        val summaryMap: Map<String, Any?> = mapOf(
            "id" to groupSummary.id,
            "name" to groupSummary.name,
            "avatar" to groupSummary.avatar,
            "notificationOn" to groupSummary.notificationOn
        )

        val updates: Map<String, Any?> = mapOf(
            "$groupRootPath/${group.id}" to groupMap,
            "$userRootPath/$userId/$userGroupsField/${group.id}" to summaryMap
        )
        val castedUpdates: Map<Any?, *> = updates.entries.associate { it.key as Any? to it.value }
        suspendCancellableCoroutine<Unit> { cont ->
            dbRoot.updateChildValues(castedUpdates) { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun prepareForFirebase(value: Any?): Any? {
        return when (value) {
            null -> null
            is Map<*, *> -> {
                val dict = NSMutableDictionary()
                value.forEach { (k, v) ->
                    if (k is String) {
                        prepareForFirebase(v)?.let { dict.setObject(it, NSString.create(string = k)) }
                    }
                }
                dict
            }
            is List<*> -> {
                val arr = NSMutableArray()
                value.forEach { item -> prepareForFirebase(item)?.let { arr.addObject(it) } }
                arr
            }
            is Boolean -> NSNumber.numberWithBool(value)
            is Int -> NSNumber.numberWithInt(value)
            is Long -> NSNumber.numberWithDouble(value.toDouble())
            is Double -> NSNumber.numberWithDouble(value)
            is Float -> NSNumber.numberWithFloat(value)
            is String -> NSString.create(string = value)
            else -> value
        }
    }

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
                        (NSString.create(string = encodedPath) as platform.Foundation.NSString)
                            .stringByRemovingPercentEncoding ?: encodedPath
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

        private suspend fun headRequestSucceeds(url: String): Boolean = suspendCancellableCoroutine { cont ->
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl == null) {
                cont.resume(false)
                return@suspendCancellableCoroutine
            }
            val request = NSMutableURLRequest(uRL = nsUrl)
            request.HTTPMethod = "HEAD"
            request.timeoutInterval = 3.0
            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { _, response, _ ->
                val code = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
                if (cont.isActive) cont.resume(code in 200..299)
            }
            task.resume()
            cont.invokeOnCancellation { task.cancel() }
        }
    }
}