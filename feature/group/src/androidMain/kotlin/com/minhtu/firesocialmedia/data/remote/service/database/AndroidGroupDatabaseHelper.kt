package com.minhtu.firesocialmedia.data.remote.service.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AndroidGroupDatabaseHelper {
    suspend fun getNew(newId: String, newsPath: String): NewsDTO? {
        val raw = suspendCoroutine<NewsDTO?> { continuation ->
            val databaseReference = FirebaseDatabase.getInstance()
                .reference
                .child(newsPath)
                .child(newId)
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    continuation.resume(snapshot.getValue(NewsDTO::class.java))
                }
                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(null)
                }
            })
        } ?: return null

        raw.avatar = resolveMediaUrlAsync(raw.avatar)
        raw.image = resolveMediaUrlAsync(raw.image)
        raw.video = resolveMediaUrlAsync(raw.video)
        return raw
    }

    fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String) {
        if (value < 0) return
        FirebaseDatabase.getInstance()
            .reference
            .child(newsPath)
            .child(newsId)
            .child(likedCountPath)
            .setValue(value)
    }

    suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean {
        return runCatching {
            FirebaseDatabase.getInstance()
                .reference
                .child(newsPath)
                .child(new.id)
                .removeValue()
                .await()

            if (new.image.isNotEmpty() || new.video.isNotEmpty()) {
                try {
                    FirebaseStorage.getInstance()
                        .reference
                        .child(newsPath)
                        .child(new.id)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    Log.w("Task", "Storage delete: ${e.message}")
                }
            }
            true
        }.getOrElse {
            false
        }
    }

    fun updateGroupDataOnServer(
        databaseRef: DatabaseReference,
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        group: GroupDTO,
        userId: String,
        continuation: CancellableContinuation<Boolean>
    ) {
        // Store only necessary fields under user
        val groupSummary = GroupSummaryDTO(
            id = group.id,
            name = group.name,
            avatar = group.avatar
        )

        val updates = hashMapOf<String, Any?>(
            "$groupRootPath/${group.id}" to group,

            "$userRootPath/$userId/$userGroupsField/${group.id}" to groupSummary
        )

        databaseRef.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener

                if (!task.isSuccessful) {
                    Log.e("Task", "updateChildren FAILED", task.exception)
                    Log.e("Task", "updates=$updates")
                } else {
                    Log.d("Task", "updateChildren SUCCESS")
                }

                continuation.resume(task.isSuccessful)
            }
    }

    suspend fun getAllGroups(
        userPath: String,
        groupPath: String,
        userId: String
    ): Set<GroupSummaryDTO> {

        val snapshot = FirebaseDatabase
            .getInstance()
            .reference
            .child(userPath)
            .child(userId)
            .child(groupPath)
            .get()
            .await()

        return snapshot.children
            .mapNotNull { it.getValue(GroupSummaryDTO::class.java) }
            .let { groups ->
                coroutineScope {
                    groups.map { group ->
                        async { group.copy(avatar = resolveMediaUrlAsync(group.avatar)) }
                    }.awaitAll()
                }
            }
            .toSet()
    }

    suspend fun fetchGroupInfo(
        groupId: String,
        groupPath: String
    ): GroupDTO? {

        val snapshot = FirebaseDatabase
            .getInstance()
            .reference
            .child(groupPath)
            .child(groupId)
            .get()
            .await()

        val group = snapshot.getValue(GroupDTO::class.java)

        return group?.copy(
            avatar = resolveMediaUrlAsync(group.avatar)
        )
    }

    suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        return runCatching {
            val databaseRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(userPath)
                .child(userId)
                .child(groupPath)
                .child(groupId)
                .child(notificationStatusPath)
            databaseRef.setValue(newStatus).await()
            true
        }.getOrElse {
            false
        }
    }

    suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String> {
        return runCatching {
            val snapshot = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(groupPath)
                .child(groupId)
                .child(membersPath)
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

    suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO {

        return try {
            val snapshot = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(userPath)
                .child(userId)
                .child(groupPath)
                .child(groupId)
                .get()
                .await()

            val group = snapshot.getValue(GroupSummaryDTO::class.java)

            group?.copy(
                avatar = resolveMediaUrlAsync(group.avatar)
            ) ?: GroupSummaryDTO()

        } catch (e: Exception) {
            GroupSummaryDTO()
        }
    }

    suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        return runCatching {
            val snapshot = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(userPath)
                .child(userId)
                .child(groupPath)
                .child(groupId)
                .child(notificationStatusPath)
                .get()
                .await()
            snapshot.getValue(Boolean::class.java) ?: false
        }.getOrElse {
            false
        }
    }

    suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean = suspendCancellableCoroutine { continuation ->

        val databaseRef = FirebaseDatabase.getInstance().reference

        val groupSummary = GroupSummaryDTO(
            id = group.id,
            name = group.name,
            avatar = group.avatar  // store raw path, not resolved URL
        )

        val updates = hashMapOf<String, Any?>(
            "$groupPath/${group.id}/$memberPath/${user.uid}" to "member",

            "$userPath/${user.uid}/$groupPath/${group.id}" to groupSummary,

            "$groupPath/${group.id}/$memberCountPath" to ServerValue.increment(1)
        )

        databaseRef.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener

                if (!task.isSuccessful) {
                    Log.e("Task", "updateChildren FAILED", task.exception)
                    Log.e("Task", "updates=$updates")
                } else {
                    Log.d("Task", "updateChildren SUCCESS")
                }

                continuation.resume(task.isSuccessful)
            }
    }

    suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val databaseRef = FirebaseDatabase.getInstance().reference


        val updates = hashMapOf(
            "$groupPath/${group.id}/$memberPath/${user.uid}" to null,

            "$userPath/${user.uid}/$groupPath/${group.id}" to null,
            "$groupPath/${group.id}/$memberCountPath" to ServerValue.increment(-1)
        )

        databaseRef.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener

                if (!task.isSuccessful) {
                    Log.e("Task", "updateChildren FAILED", task.exception)
                    Log.e("Task", "updates=$updates")
                } else {
                    Log.d("Task", "updateChildren SUCCESS")
                }

                continuation.resume(task.isSuccessful)
            }
    }

    suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val databaseRef = FirebaseDatabase.getInstance().reference


        val updates = hashMapOf<String, Any?>(
            "$groupPath/${group.id}" to null,

            "$userPath/${user.uid}/$groupPath/${group.id}" to null
        )

        databaseRef.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener

                if (!task.isSuccessful) {
                    Log.e("Task", "updateChildren FAILED", task.exception)
                    Log.e("Task", "updates=$updates")
                } else {
                    Log.d("Task", "updateChildren SUCCESS")
                }

                continuation.resume(task.isSuccessful)
            }
    }

    suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val databaseRef = FirebaseDatabase
            .getInstance()
            .reference
            .child(groupPath)
            .child(group.id)
            .child(memberPath)
            .child(user.uid)
        databaseRef.setValue(role).addOnCompleteListener { task ->
            continuation.resume(task.isSuccessful)
        }
    }

    suspend fun fetchGroupsByMemberCount(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO> = suspendCancellableCoroutine { continuation ->

        val databaseRef = FirebaseDatabase
            .getInstance()
            .reference
            .child(groupPath)

        databaseRef
            .orderByChild(memberCountPath)
            .limitToLast(limit)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val rawList = snapshot.children
                        .mapNotNull { it.getValue(GroupDTO::class.java) }
                        .sortedByDescending { it.memberCount }

                    if (continuation.isActive) {
                        continuation.resume(rawList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (continuation.isActive) {
                        continuation.resume(emptyList())
                    }
                }
            })
    }

    suspend fun createPoll(
        poll: PollDTO,
        pollPath: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        newsId: String,
        newsPosterId: String,
        newsPosterName: String,
        newsAvatar: String,
        newsMessage: String,
        newsLikeCount: Int,
        newsCommentCount: Int,
        newsTimePosted: Long,
        newsType: String?,
        newsPollId: String?
    ): Boolean = suspendCancellableCoroutine { continuation ->
        Log.d("Task", "createPoll: ${poll.id} in group $groupId")
        val databaseRef = FirebaseDatabase.getInstance().reference
        // Index entry lives under /groups/{groupId}/posts/{newsId}  (same path as regular group posts)
        // Full poll data lives under /polls/{pollId}
        val newsEntry = hashMapOf<String, Any?>(
            "id" to newsId,
            "posterId" to newsPosterId,
            "posterName" to newsPosterName,
            "avatar" to newsAvatar,
            "message" to newsMessage,
            "image" to "",
            "video" to "",
            "isVisible" to true,
            "likeCount" to newsLikeCount,
            "commentCount" to newsCommentCount,
            "timePosted" to newsTimePosted,
            "localPath" to "",
            "shareContentId" to "",
            "decentralizationType" to "",
            "type" to newsType,
            "pollId" to newsPollId
        )
        val updates = hashMapOf<String, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to newsEntry,
            "$pollPath/${poll.id}" to poll
        )
        databaseRef.updateChildren(updates).addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                Log.d("Task", "createPoll success")
            } else {
                Log.e("Task", "createPoll FAILED", task.exception)
            }
            continuation.resume(task.isSuccessful)
        }
    }

    suspend fun deletePollFromDatabase(
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

    suspend fun fetchPoll(pollId: String, pollPath: String): PollDTO? {
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

    suspend fun loadMyVotes(
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

    suspend fun loadAllVoters(
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

    suspend fun submitVote(
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
}
