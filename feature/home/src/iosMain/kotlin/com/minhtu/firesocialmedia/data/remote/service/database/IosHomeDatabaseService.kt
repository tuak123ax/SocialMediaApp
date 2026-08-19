package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.constants.home.Constants
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.home.IosDatabaseHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.home.SupabaseStorage
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithDouble
import platform.Foundation.numberWithFloat
import platform.Foundation.numberWithInt
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private fun Map<String, Any?>.toHomeNewsDTO(): NewsDTO {
    return NewsDTO(
        id = this["id"] as? String ?: "",
        posterId = this["posterId"] as? String ?: "",
        posterName = this["posterName"] as? String ?: "",
        avatar = this["avatar"] as? String ?: "",
        message = this["message"] as? String ?: "",
        image = this["image"] as? String ?: "",
        video = this["video"] as? String ?: "",
        isVisible = this["isVisible"] as? Boolean ?: true,
        likeCount = (this["likeCount"] as? Long)?.toInt() ?: 0,
        commentCount = (this["commentCount"] as? Long)?.toInt() ?: 0,
        timePosted = this["timePosted"] as? Long ?: 0
    )
}

private fun Map<*, *>.toHomeUserDTO(): UserDTO {
    val likedPosts = (this["likedPosts"] as? Map<*, *>)?.mapNotNull { entry ->
        val key = entry.key as? String
        val value = when (val rawValue = entry.value) {
            is Number -> rawValue.toInt()
            else -> null
        }
        if (key != null && value != null) key to value else null
    }?.toMap()?.let { HashMap(it) } ?: HashMap()

    val likedComments = (this["likedComments"] as? Map<*, *>)?.mapNotNull { entry ->
        val key = entry.key as? String
        val value = when (val rawValue = entry.value) {
            is Number -> rawValue.toInt()
            else -> null
        }
        if (key != null && value != null) key to value else null
    }?.toMap()?.let { HashMap(it) } ?: HashMap()

    val friendRequests = (this["friendRequests"] as? List<*>)?.mapNotNull { it as? String }
        ?.let { ArrayList(it) } ?: ArrayList()

    val friends = (this["friends"] as? List<*>)?.mapNotNull { it as? String }
        ?.let { ArrayList(it) } ?: ArrayList()

    return UserDTO(
        email = this["email"] as? String ?: "",
        image = this["image"] as? String ?: "",
        name = this["name"] as? String ?: "",
        status = this["status"] as? String ?: "",
        token = this["token"] as? String ?: "",
        uid = this["uid"] as? String ?: "",
        likedPosts = likedPosts,
        friendRequests = friendRequests,
        friends = friends,
        likedComments = likedComments
    )
}

/**
 * iOS implementation of feature/home's own [HomeDatabaseService], moved out of core's
 * `IosDatabaseService`/`SupabaseStorageHelper` so that core no longer needs to know about
 * News-typed data. Media (image/video) I/O still goes through core's generic `SupabaseStorage`
 * object and reuses core's `SupabaseStorageHelper` companion functions (`resolveMediaUrlAsync`,
 * `extractRelativePath`) which remain generic, reusable helpers not specific to News.
 */
class IosHomeDatabaseService : HomeDatabaseService {
    private val database: FIRDatabaseReference
        get() = FIRDatabase.database().reference()

    override suspend fun getNew(newId: String): NewsDTO? {
        val rawNews = suspendCancellableCoroutine<NewsDTO?> { continuation ->
            val databaseReference = FIRDatabase.database().reference()
                .child(DataConstant.NEWS_PATH)
                .child(newId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val rawValue = snapshot.value as? Map<*, *>
                        if (rawValue != null) {
                            try {
                                val value = rawValue.entries.associate {
                                    (it.key as? String) to it.value
                                }.filterKeys { it != null } as Map<String, Any?>

                                val news = value.toHomeNewsDTO()
                                if (continuation.isActive) continuation.resume(news) {}
                            } catch (_: Exception) {
                                if (continuation.isActive) continuation.resume(null) {}
                            }
                        } else {
                            if (continuation.isActive) continuation.resume(null) {}
                        }
                    } else {
                        if (continuation.isActive) continuation.resume(null) {}
                    }
                }
            ) { _ -> if (continuation.isActive) continuation.resume(null) {} }
        } ?: return null

        if (rawNews.avatar.isNotEmpty()) rawNews.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(rawNews.avatar)
        if (rawNews.image.isNotEmpty()) rawNews.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawNews.image))
        if (rawNews.video.isNotEmpty()) rawNews.updateVideo(SupabaseStorageHelper.resolveMediaUrlAsync(rawNews.video))
        return rawNews
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsDTO {
        val raw = suspendCancellableCoroutine<LatestNewsDTO> { continuation ->
            val query = FIRDatabase.database()
                .referenceWithPath(path)
                .queryOrderedByChild("timePosted")
                .let { base ->
                    if (lastTimePosted != null && lastKey != null) {
                        base.queryEndingBeforeValue(lastTimePosted, childKey = lastKey)
                    } else base
                }
                .queryLimitedToLast(number.toULong())

            query.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    val enumerator = snapshot?.children
                    val newsList = mutableListOf<NewsDTO>()
                    if (enumerator != null) {
                        while (true) {
                            val child = enumerator.nextObject() as? FIRDataSnapshot ?: break
                            val childRaw = child.value as? Map<*, *> ?: continue
                            val value = childRaw.entries
                                .associate { (k, v) -> (k as? String) to v }
                                .filterKeys { it != null } as Map<String, Any?>
                            try {
                                newsList.add(value.toHomeNewsDTO())
                            } catch (_: Exception) {
                            }
                        }
                    }

                    if (newsList.isNotEmpty()) {
                        val sorted = newsList.sortedByDescending { it.timePosted }
                        val oldest = sorted.last()
                        if (continuation.isActive) continuation.resume(
                            LatestNewsDTO(
                                sorted,
                                if (newsList.size < number) null else oldest.timePosted.toDouble(),
                                oldest.id
                            )
                        )
                    } else {
                        if (continuation.isActive) continuation.resume(
                            LatestNewsDTO(emptyList(), null, null)
                        )
                    }
                }
            ) { _ ->
                if (continuation.isActive) continuation.resume(LatestNewsDTO(null, null, null))
            }
        }

        val resolvedNews = raw.news?.let { list ->
            coroutineScope {
                list.map { news ->
                    async {
                        if (news.avatar.isNotEmpty()) news.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(news.avatar)
                        if (news.image.isNotEmpty()) news.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(news.image))
                        if (news.video.isNotEmpty()) news.updateVideo(SupabaseStorageHelper.resolveMediaUrlAsync(news.video))
                        news
                    }
                }.awaitAll()
            }
        }
        return LatestNewsDTO(resolvedNews, raw.lastTimePostedValue, raw.lastKeyValue)
    }

    override suspend fun deleteNewsFromDatabase(path: String, new: NewsDTO) {
        logMessage("IosHomeDatabaseService", { "deleteNewsFromDatabase" })
        try {
            val dbRef = database.child(path).child(new.id)
            removeValueSuspend(dbRef)
        } catch (e: Exception) {
            logMessage("IosHomeDatabaseService", { "DB delete failed: ${e.message}" })
        }
        try {
            when {
                new.image.isNotEmpty() -> SupabaseStorage.delete(new.image)
                new.video.isNotEmpty() -> SupabaseStorage.delete(new.video)
            }
        } catch (e: Exception) {
            logMessage("IosHomeDatabaseService", { "Storage delete failed: ${e.message}" })
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
        logMessage("IosHomeDatabaseService", { "updateNewsFromDatabase" })
        val dbRef = database.child(path).child(new.id)
        return try {
            val updates = mutableMapOf<String, Any>("message" to newContent)
            when {
                newImage.isNotEmpty() -> {
                    if (newImage != new.image) {
                        val bytes = Base64.Default.decode(newImage)
                        val remotePath = "$path/${new.id}_${getCurrentTime()}.jpg"
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
                        val remotePath = "$path/${new.id}_${getCurrentTime()}.mp4"
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
            logMessage("IosHomeDatabaseService", { "updateNewsFromDatabase failed: ${t.message}" })
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
                    val remotePath = "$path/${commentId}_${getCurrentTime()}.jpg"
                    SupabaseStorage.uploadBytes(bytes, remotePath)
                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }
                instance.video.isNotEmpty() -> {
                    val sourcePath = instance.localPath.ifEmpty { instance.video }
                    val remotePath = "$path/${commentId}_${getCurrentTime()}.mp4"
                    SupabaseStorage.uploadFile(sourcePath, remotePath)
                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }
                else -> { /* no media */ }
            }
            setValueSuspend(dbRef, instance.toMap())
            true
        }.getOrElse { e ->
            logMessage("IosHomeDatabaseService", { "saveNewToDatabase failed: ${e.message}" })
            false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveNewToGroup(groupId: String, instance: NewsDTO): Boolean {
        return runCatching {
            val dbRef = database
                .child(DataConstant.GROUP_PATH)
                .child(groupId)
                .child(DataConstant.POSTS_PATH)
                .child(instance.id)

            when {
                instance.image.isNotEmpty() -> {
                    val sourceBase64 = instance.localPath.ifEmpty { instance.image }
                    val bytes = Base64.Default.decode(sourceBase64)
                    val remotePath =
                        "${DataConstant.GROUP_PATH}/$groupId/${DataConstant.IMAGE_PATH}/${instance.id}_${getCurrentTime()}.jpg"
                    SupabaseStorage.uploadBytes(bytes, remotePath)
                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }
                instance.video.isNotEmpty() -> {
                    val sourcePath = instance.localPath.ifEmpty { instance.video }
                    val remotePath =
                        "${DataConstant.GROUP_PATH}/$groupId/${DataConstant.IMAGE_PATH}/${instance.id}_${getCurrentTime()}.mp4"
                    SupabaseStorage.uploadFile(sourcePath, remotePath)
                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }
                else -> { /* no media */ }
            }

            setValueSuspend(dbRef, instance.toMap())
            true
        }.getOrElse { e ->
            logMessage("IosHomeDatabaseService", { "saveNewToGroup failed: ${e.message}" })
            false
        }
    }

    override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> =
        suspendCancellableCoroutine { cont ->
            val ref = database
                .child(DataConstant.GROUP_PATH)
                .child(groupId)
                .child(DataConstant.MEMBERS_PATH)
            ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                val result = HashMap<String, String>()
                if (snapshot != null && snapshot.exists()) {
                    val children = snapshot.children
                    while (true) {
                        val child = children.nextObject() as? FIRDataSnapshot ?: break
                        val key = child.key ?: continue
                        val value = child.value as? String ?: continue
                        result[key] = value
                    }
                }
                if (cont.isActive) cont.resume(result) {}
            }) { _ -> if (cont.isActive) cont.resume(HashMap()) {} }
        }

    override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean =
        suspendCancellableCoroutine { cont ->
            val ref = database
                .child(DataConstant.USER_PATH)
                .child(userId)
                .child(DataConstant.GROUP_PATH)
                .child(groupId)
                .child(DataConstant.NOTIFICATION_STATUS_PATH)
            ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                val value = (snapshot?.value as? Boolean) ?: false
                if (cont.isActive) cont.resume(value) {}
            }) { _ -> if (cont.isActive) cont.resume(false) {} }
        }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to null,
            "$pollPath/$pollId" to null,
            "$pollVotesPath/$pollId" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null) {}
        }
    }

    override suspend fun fetchPoll(pollId: String, pollPath: String): PollDTO? = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(pollPath).child(pollId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val map = snapshot.value as? Map<*, *>
                if (map != null) {
                    val optionsRaw = map["options"]
                    val options: List<String> = when (optionsRaw) {
                        is List<*> -> optionsRaw.mapNotNull { it as? String }
                        is Map<*, *> -> optionsRaw.values.mapNotNull { it as? String }
                        else -> emptyList()
                    }
                    val votesRaw = map["votes"] as? Map<*, *>
                    val votes: Map<String, Int>? = votesRaw?.mapNotNull { (k, v) ->
                        val key = k as? String ?: return@mapNotNull null
                        val value = (v as? Long)?.toInt() ?: (v as? Int) ?: return@mapNotNull null
                        key to value
                    }?.toMap()
                    val poll = PollDTO(
                        id = map["id"] as? String ?: pollId,
                        posterId = map["posterId"] as? String ?: "",
                        posterName = map["posterName"] as? String ?: "",
                        posterAvatar = map["posterAvatar"] as? String ?: "",
                        question = map["question"] as? String ?: "",
                        options = options,
                        allowMultipleAnswers = map["allowMultipleAnswers"] as? Boolean ?: false,
                        duration = map["duration"] as? String ?: "",
                        groupId = map["groupId"] as? String ?: "",
                        likeCount = (map["likeCount"] as? Long)?.toInt() ?: 0,
                        commentCount = (map["commentCount"] as? Long)?.toInt() ?: 0,
                        timePosted = (map["timePosted"] as? Long) ?: 0L,
                        expiresAt = map["expiresAt"] as? Long,
                        votes = votes
                    )
                    if (cont.isActive) cont.resume(poll) {}
                } else {
                    if (cont.isActive) cont.resume(null) {}
                }
            } else {
                if (cont.isActive) cont.resume(null) {}
            }
        }) { _ -> if (cont.isActive) cont.resume(null) {} }
    }

    override suspend fun loadMyVotes(
        pollId: String,
        userId: String,
        pollVotesPath: String
    ): List<Int> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(pollVotesPath).child(pollId).child(userId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                val list = mutableListOf<Int>()
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    (child.value as? Long)?.toInt()?.let { list.add(it) }
                }
                list
            } else emptyList()
            if (cont.isActive) cont.resume(result) {}
        }) { _ -> if (cont.isActive) cont.resume(emptyList()) {} }
    }

    override suspend fun loadAllVoters(pollId: String, pollVotesPath: String): Map<String, List<Int>> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(pollVotesPath).child(pollId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = mutableMapOf<String, List<Int>>()
            if (snapshot != null && snapshot.exists()) {
                val users = snapshot.children
                while (true) {
                    val userSnapshot = users.nextObject() as? FIRDataSnapshot ?: break
                    val uid = userSnapshot.key ?: continue
                    val indices = mutableListOf<Int>()
                    val voteChildren = userSnapshot.children
                    while (true) {
                        val child = voteChildren.nextObject() as? FIRDataSnapshot ?: break
                        (child.value as? Long)?.toInt()?.let { indices.add(it) }
                    }
                    result[uid] = indices
                }
            }
            if (cont.isActive) cont.resume(result) {}
        }) { _ -> if (cont.isActive) cont.resume(emptyMap()) {} }
    }

    override suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$pollVotesPath/$pollId/$userId" to selectedIndices
        )
        // We need to update vote counts by reading current values first
        val pollRef = dbRef.child(pollPath).child(pollId).child("votes")
        pollRef.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val currentVotes = (snapshot?.value as? Map<*, *>)?.mapNotNull { (k, v) ->
                val key = k as? String ?: return@mapNotNull null
                val value = (v as? Long)?.toInt() ?: return@mapNotNull null
                key to value
            }?.toMap()?.toMutableMap() ?: mutableMapOf()

            previousIndices.forEach { idx ->
                if (!selectedIndices.contains(idx)) {
                    val current = currentVotes[idx.toString()] ?: 0
                    currentVotes[idx.toString()] = maxOf(0, current - 1)
                }
            }
            selectedIndices.forEach { idx ->
                if (!previousIndices.contains(idx)) {
                    val current = currentVotes[idx.toString()] ?: 0
                    currentVotes[idx.toString()] = current + 1
                }
            }
            currentVotes.forEach { (k, v) -> updates["$pollPath/$pollId/votes/$k"] = v }
            dbRef.updateChildValues(updates) { error, _ ->
                if (cont.isActive) cont.resume(error == null) {}
            }
        }) { _ -> if (cont.isActive) cont.resume(false) {} }
    }

    override suspend fun getUser(userId: String): UserDTO? {
        val rawUser = suspendCancellableCoroutine<UserDTO?> { continuation ->
            val databaseReference = database
                .child(DataConstant.USER_PATH)
                .child(userId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val value = snapshot.value as? Map<*, *> ?: null
                        if (value != null) {
                            try {
                                val user = value.toHomeUserDTO()
                                continuation.resume(user) {}
                            } catch (_: Exception) {
                                continuation.resume(null) {}
                            }
                        } else {
                            continuation.resume(null) {}
                        }
                    } else {
                        continuation.resume(null) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        // Phase 2: resolve user avatar
        if (rawUser.image.isNotEmpty()) rawUser.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawUser.image))
        return rawUser
    }

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserDTO>? {
        val rawList = suspendCancellableCoroutine<List<UserDTO>?> { continuation ->
            val databaseReference = database.child(DataConstant.USER_PATH)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val users = mutableListOf<UserDTO>()
                        val children = snapshot.children

                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val value = child.value as? Map<*, *> ?: continue

                            try {
                                val user = value.toHomeUserDTO()
                                if (user.name.contains(name, ignoreCase = true)) {
                                    users.add(user)
                                    if (users.size >= 5) break // only return first 5 matches
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }

                        continuation.resume(users) {}
                    } else {
                        continuation.resume(emptyList<UserDTO>()) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        return coroutineScope {
            rawList.map { user ->
                async {
                    if (user.image.isNotEmpty()) user.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(user.image))
                    user
                }
            }.awaitAll()
        }
    }

    override suspend fun updateFCMTokenForCurrentUser(currentUser: UserDTO) {
        val currentFCMToken = IosCryptoHelper.getFromKeychain(Constants.KEY_FCM_TOKEN) ?: ""
        if (currentFCMToken.isNotEmpty()) {
            if (currentUser.token != currentFCMToken) {
                currentUser.token = currentFCMToken
                IosDatabaseHelper.saveStringToDatabase(currentUser.uid, DataConstant.USER_PATH, currentFCMToken, DataConstant.TOKEN_PATH)
            }
        }
    }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return IosDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        IosDatabaseHelper.updateCountValueInDatabase(id, path, externalPath, value)
    }

    // --- Private helpers (mirroring core's SupabaseStorageHelper private helpers) ---

    private suspend fun deleteOldMediaIfNeeded(path: String) {
        if (path.isEmpty()) return
        val cleanPath = SupabaseStorageHelper.extractRelativePath(path) ?: return
        try {
            SupabaseStorage.delete(cleanPath)
        } catch (e: Exception) {
            logMessage("IosHomeDatabaseService", { "deleteOldMediaIfNeeded error: ${e.message}" })
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
}
