package com.minhtu.firesocialmedia.data.remote.service.database

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.AndroidCryptoHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.home.AndroidDatabaseHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.home.SupabaseStorage
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.constants.home.Constants
import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android implementation of feature/home's own [HomeDatabaseService], moved out of core's
 * `AndroidDatabaseService`/`SupabaseStorageHelper` so that core no longer needs to know about
 * News-typed data. Media (image/video) I/O still goes through core's generic `SupabaseStorage`
 * object and reuses core's [SupabaseStorageHelper] instance methods (`getFileExtension`,
 * `deleteOldMediaIfNeeded`) which remain generic, reusable helpers not specific to News.
 */
class AndroidHomeDatabaseService(context: Context) : HomeDatabaseService {
    // Never hold a Service context to avoid leaks; keep only applicationContext
    private val appContext: Context = context.applicationContext
    private val fileHelper = SupabaseStorageHelper()

    override suspend fun getNew(newId: String): NewsDTO? {
        // Phase 1: fetch raw news from Firebase
        val raw = withTimeout(5000) {
            suspendCoroutine<NewsDTO?> { continuation ->
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference()
                    .child(DataConstant.NEWS_PATH)
                    .child(newId)
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot.getValue(NewsDTO::class.java))
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        // Phase 2: async resolve media URLs
        raw.avatar = resolveMediaUrlAsync(raw.avatar)
        raw.image = resolveMediaUrlAsync(raw.image)
        raw.video = resolveMediaUrlAsync(raw.video)
        return raw
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsDTO {
        // Phase 1: fetch raw news list from Firebase (sync, no network calls)
        val raw = suspendCancellableCoroutine { continuation ->
            val query = FirebaseDatabase.getInstance()
                .getReference(path)
                .orderByChild("timePosted")
                .let { q ->
                    when {
                        lastTimePosted != null && !lastKey.isNullOrBlank() -> q.endBefore(lastTimePosted, lastKey)
                        lastTimePosted != null -> q.endBefore(lastTimePosted)
                        else -> q
                    }
                }
                .limitToLast(number)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!continuation.isActive) return
                    val newsList = snapshot.children.mapNotNull { it.getValue(NewsDTO::class.java) }
                    if (newsList.isEmpty()) {
                        query.removeEventListener(this)
                        continuation.resume(LatestNewsDTO())
                        return
                    }
                    val sorted = newsList.sortedByDescending { it.timePosted }
                    val oldest = sorted.last()
                    query.removeEventListener(this)
                    continuation.resume(
                        LatestNewsDTO(
                            news = sorted,
                            lastTimePostedValue = if (newsList.size < number) null else oldest.timePosted.toDouble(),
                            lastKeyValue = oldest.id
                        )
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    if (!continuation.isActive) return
                    query.removeEventListener(this)
                    continuation.resume(LatestNewsDTO())
                }
            }
            query.addValueEventListener(listener)
            continuation.invokeOnCancellation { query.removeEventListener(listener) }
        }

        if (raw.news.isNullOrEmpty()) return raw

        // Phase 2: resolve all post media URLs in parallel (one coroutine per post)
        val resolved = coroutineScope {
            raw.news.map { news ->
                async {
                    news.copy(
                        avatar = resolveMediaUrlAsync(news.avatar),
                        image = resolveMediaUrlAsync(news.image),
                        video = resolveMediaUrlAsync(news.video)
                    )
                }
            }.awaitAll()
        }
        return raw.copy(news = resolved)
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
                    SupabaseStorage.delete(new.image)
                }

                new.video.isNotEmpty() -> {
                    SupabaseStorage.delete(new.video)
                }
            }
        } catch (e: Exception) {
            Log.w("Task", "Supabase Storage delete: ${e.message}")
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
                    val extension = fileHelper.getFileExtension(filePath, "jpg")

                    val remotePath =
                        "$path/${commentId}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }

                instance.video.isNotEmpty() -> {
                    val filePath = instance.localPath.ifEmpty { instance.video }
                    val extension = fileHelper.getFileExtension(filePath, "mp4")

                    val remotePath =
                        "$path/${commentId}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }

                else -> {
                    // No media -> just save
                }
            }

            dbRef.setValue(instance).await()
            true

        }.getOrElse { e ->
            Log.e("Task", "saveNewToDatabase failed: ${e.message}", e)
            false
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

                        val extension = fileHelper.getFileExtension(newImage, "jpg")
                        val remotePath =
                            "$path/${new.id}_${System.currentTimeMillis()}.$extension"

                        SupabaseStorage.upload(
                            filePath = newImage,
                            remotePath = remotePath
                        )

                        fileHelper.deleteOldMediaIfNeeded(new.image)

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

                        val extension = fileHelper.getFileExtension(newVideo, "mp4")
                        val remotePath =
                            "$path/${new.id}_${System.currentTimeMillis()}.$extension"

                        SupabaseStorage.upload(
                            filePath = newVideo,
                            remotePath = remotePath
                        )

                        fileHelper.deleteOldMediaIfNeeded(new.video)

                        updates["video"] = remotePath
                        updates["image"] = ""
                    } else {
                        updates["video"] = new.video
                        updates["image"] = ""
                    }
                }

                // No media -> clear & delete old file
                else -> {
                    updates["image"] = ""
                    updates["video"] = ""

                    fileHelper.deleteOldMediaIfNeeded(new.image)
                    fileHelper.deleteOldMediaIfNeeded(new.video)
                }
            }

            dbRef.updateChildren(updates).await()
            true

        } catch (t: Throwable) {
            Log.e("Task", "updateNewsFromDatabase failed", t)
            false
        }
    }

    override suspend fun saveNewToGroup(groupId: String, instance: NewsDTO): Boolean {
        return runCatching {
            val dbRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(DataConstant.GROUP_PATH)
                .child(groupId)
                .child(DataConstant.POSTS_PATH)
                .child(instance.id)

            when {
                instance.image.isNotEmpty() -> {
                    val filePath = instance.localPath.ifEmpty { instance.image }
                    val extension = fileHelper.getFileExtension(filePath, "jpg")

                    val remotePath =
                        "${DataConstant.GROUP_PATH}/$groupId/${DataConstant.IMAGE_PATH}/${instance.id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    instance.updateImage(remotePath)
                    instance.updateVideo("")
                }

                instance.video.isNotEmpty() -> {
                    val filePath = instance.localPath.ifEmpty { instance.video }
                    val extension = fileHelper.getFileExtension(filePath, "mp4")

                    val remotePath =
                        "${DataConstant.GROUP_PATH}/$groupId/${DataConstant.IMAGE_PATH}/${instance.id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    instance.updateVideo(remotePath)
                    instance.updateImage("")
                }

                else -> {
                    // No media -> just save
                }
            }

            dbRef.setValue(instance).await()
            true

        }.getOrElse { e ->
            Log.e("Task", "saveNewToGroup failed: ${e.message}", e)
            false
        }
    }

    override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> {
        return runCatching {
            val snapshot = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(DataConstant.GROUP_PATH)
                .child(groupId)
                .child(DataConstant.MEMBERS_PATH)
                .get()
                .await()

            val result = HashMap<String, String>()
            for (child in snapshot.children) {
                val key = child.key ?: continue
                val value = child.getValue(String::class.java) ?: continue
                result[key] = value
            }
            result
        }.getOrElse {
            HashMap()
        }
    }

    override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean {
        return runCatching {
            val snapshot = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(DataConstant.USER_PATH)
                .child(userId)
                .child(DataConstant.GROUP_PATH)
                .child(groupId)
                .child(DataConstant.NOTIFICATION_STATUS_PATH)
                .get()
                .await()
            snapshot.getValue(Boolean::class.java) ?: false
        }.getOrElse {
            false
        }
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        Log.d("Task", "deletePollFromDatabase: newsId=$newsId pollId=$pollId groupId=$groupId")
        val databaseRef = FirebaseDatabase.getInstance().reference
        val updates = hashMapOf<String, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to null,
            "$pollPath/$pollId" to null,
            "$pollVotesPath/$pollId" to null
        )
        databaseRef.updateChildren(updates).addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                Log.d("Task", "deletePollFromDatabase success")
            } else {
                Log.e("Task", "deletePollFromDatabase FAILED", task.exception)
            }
            continuation.resume(task.isSuccessful)
        }
    }

    override suspend fun fetchPoll(pollId: String, pollPath: String): PollDTO? {
        return try {
            val snapshot = FirebaseDatabase.getInstance().reference
                .child(pollPath)
                .child(pollId)
                .get()
                .await()
            if (!snapshot.exists()) return null
            // Manually parse snapshot to avoid @Serializable interference with Firebase reflection
            val id = snapshot.child("id").getValue(String::class.java) ?: ""
            val posterId = snapshot.child("posterId").getValue(String::class.java) ?: ""
            val posterName = snapshot.child("posterName").getValue(String::class.java) ?: ""
            val posterAvatar = snapshot.child("posterAvatar").getValue(String::class.java) ?: ""
            val question = snapshot.child("question").getValue(String::class.java) ?: ""
            val allowMultipleAnswers = snapshot.child("allowMultipleAnswers").getValue(Boolean::class.java) ?: false
            val duration = snapshot.child("duration").getValue(String::class.java) ?: ""
            val groupId = snapshot.child("groupId").getValue(String::class.java) ?: ""
            val likeCount = (snapshot.child("likeCount").getValue(Long::class.java) ?: 0L).toInt()
            val commentCount = (snapshot.child("commentCount").getValue(Long::class.java) ?: 0L).toInt()
            val timePosted = snapshot.child("timePosted").getValue(Long::class.java) ?: 0L
            val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java)
            // Parse options: stored as Firebase array {"0":"opt1","1":"opt2"} or list
            val optionsSnapshot = snapshot.child("options")
            val options: List<String> = if (optionsSnapshot.exists()) {
                optionsSnapshot.children.mapNotNull { it.getValue(String::class.java) }
            } else emptyList()
            // Parse votes: stored as {"0": count0, "1": count1, ...}
            val votesSnapshot = snapshot.child("votes")
            val votes: Map<String, Int>? = if (votesSnapshot.exists()) {
                votesSnapshot.children.associate { child ->
                    val key = child.key ?: ""
                    val value = (child.getValue(Long::class.java) ?: 0L).toInt()
                    key to value
                }
            } else null
            Log.d("Task", "fetchPoll $pollId: options=$options, votes=$votes")
            PollDTO(
                id = id,
                posterId = posterId,
                posterName = posterName,
                posterAvatar = posterAvatar,
                question = question,
                options = options,
                allowMultipleAnswers = allowMultipleAnswers,
                duration = duration,
                groupId = groupId,
                likeCount = likeCount,
                commentCount = commentCount,
                timePosted = timePosted,
                expiresAt = expiresAt,
                votes = votes
            )
        } catch (e: Exception) {
            Log.e("Task", "fetchPoll failed", e)
            null
        }
    }

    override suspend fun loadMyVotes(
        pollId: String,
        userId: String,
        pollVotesPath: String
    ): List<Int> {
        return try {
            val snapshot = FirebaseDatabase.getInstance().reference
                .child(pollVotesPath)
                .child(pollId)
                .child(userId)
                .get()
                .await()
            if (!snapshot.exists()) return emptyList()
            // Stored as a list of Longs (Firebase JSON array) or map {0:true}
            snapshot.children.mapNotNull { child ->
                (child.getValue(Long::class.java))?.toInt()
            }
        } catch (e: Exception) {
            Log.e("Task", "loadMyVotes failed", e)
            emptyList()
        }
    }

    override suspend fun loadAllVoters(
        pollId: String,
        pollVotesPath: String
    ): Map<String, List<Int>> {
        return try {
            val snapshot = FirebaseDatabase.getInstance().reference
                .child(pollVotesPath)
                .child(pollId)
                .get()
                .await()
            if (!snapshot.exists()) return emptyMap()
            val result = mutableMapOf<String, List<Int>>()
            for (userSnapshot in snapshot.children) {
                val uid = userSnapshot.key ?: continue
                val indices = userSnapshot.children.mapNotNull { child ->
                    (child.getValue(Long::class.java))?.toInt()
                }
                result[uid] = indices
            }
            result
        } catch (e: Exception) {
            Log.e("Task", "loadAllVoters failed", e)
            emptyMap()
        }
    }

    override suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val databaseRef = FirebaseDatabase.getInstance().reference
        val updates = hashMapOf<String, Any?>(
            // Overwrite this user's vote record
            "$pollVotesPath/$pollId/$userId" to selectedIndices
        )
        // Decrement counts for options the user is unselecting
        previousIndices.forEach { idx ->
            if (!selectedIndices.contains(idx)) {
                updates["$pollPath/$pollId/votes/$idx"] = ServerValue.increment(-1)
            }
        }
        // Increment counts for newly selected options
        selectedIndices.forEach { idx ->
            if (!previousIndices.contains(idx)) {
                updates["$pollPath/$pollId/votes/$idx"] = ServerValue.increment(1)
            }
        }
        databaseRef.updateChildren(updates).addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (!task.isSuccessful) {
                Log.e("Task", "submitVote FAILED", task.exception)
            }
            continuation.resume(task.isSuccessful)
        }
    }

    override suspend fun getUser(userId: String): UserDTO? {
        // Phase 1: fetch raw user from Firebase
        val raw = withTimeout(5000) {
            suspendCoroutine<UserDTO?> { continuation ->
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference()
                    .child(DataConstant.USER_PATH)
                    .child(userId)
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot.getValue(UserDTO::class.java))
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        // Phase 2: resolve user image URL
        raw.image = resolveMediaUrlAsync(raw.image)
        return raw
    }

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserDTO>? {
        // Phase 1: fetch raw users from Firebase
        val raw = withTimeout(5000) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference(path)
            suspendCoroutine<List<UserDTO>?> { continuation ->
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.children
                            .mapNotNull { it.getValue(UserDTO::class.java) }
                            .filter { it.name.contains(name, ignoreCase = true) }
                            .take(5)
                        continuation.resume(users)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        // Phase 2: async resolve image URLs in parallel
        return coroutineScope {
            raw.map { user ->
                async { user.copy(image = resolveMediaUrlAsync(user.image)) }
            }.awaitAll()
        }
    }

    override suspend fun updateFCMTokenForCurrentUser(currentUser: UserDTO) {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(appContext)
        val currentFCMToken = secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")
        if (!currentFCMToken.isNullOrEmpty()) {
            if (currentUser.token != currentFCMToken) {
                currentUser.token = currentFCMToken
                AndroidDatabaseHelper.saveStringToDatabase(
                    currentUser.uid,
                    DataConstant.USER_PATH,
                    currentFCMToken,
                    DataConstant.TOKEN_PATH
                )
            }
        }
    }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return AndroidDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        AndroidDatabaseHelper.updateCountValueInDatabase(id, path, externalPath, value)
    }
}
