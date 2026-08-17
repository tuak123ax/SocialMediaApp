package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.constants.group.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.group.IosDatabaseHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private fun Map<String, Any?>.toGroupNewsDTO(): NewsDTO {
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
        timePosted = this["timePosted"] as? Long ?: 0,
        localPath = this["localPath"] as? String ?: "",
        shareContentId = this["shareContentId"] as? String ?: "",
        decentralizationType = this["decentralizationType"] as? String ?: "",
        type = this["type"] as? String,
        pollId = this["pollId"] as? String
    )
}

private fun Map<*, *>.toGroupUserDTO(): UserDTO {
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

class IosGroupDatabaseService : GroupDatabaseService {
    override suspend fun getUser(userId: String): UserDTO? {
        val rawUser = suspendCancellableCoroutine<UserDTO?> { continuation ->
            val databaseReference = FIRDatabase.database().reference()
                .child(DataConstant.USER_PATH)
                .child(userId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val value = snapshot.value as? Map<*, *> ?: null
                        if (value != null) {
                            try {
                                val user = value.toGroupUserDTO()
                                continuation.resume(user)
                            } catch (_: Exception) {
                                continuation.resume(null)
                            }
                        } else {
                            continuation.resume(null)
                        }
                    } else {
                        continuation.resume(null)
                    }
                }
            ) { _ -> continuation.resume(null) }
        } ?: return null

        if (rawUser.image.isNotEmpty()) rawUser.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawUser.image))
        return rawUser
    }

    override suspend fun getNew(newId: String, newsPath: String): NewsDTO? {
        val raw = suspendCancellableCoroutine<NewsDTO?> { continuation ->
            val databaseReference = FIRDatabase.database().reference()
                .child(newsPath)
                .child(newId)
            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    val rawValue = (snapshot?.takeIf { it.exists() }?.value as? Map<*, *>)
                    if (rawValue != null) {
                        try {
                            val value = rawValue.entries.associate {
                                (it.key as? String) to it.value
                            }.filterKeys { it != null } as Map<String, Any?>
                            if (continuation.isActive) continuation.resume(value.toGroupNewsDTO()) {}
                        } catch (_: Exception) {
                            if (continuation.isActive) continuation.resume(null) {}
                        }
                    } else {
                        if (continuation.isActive) continuation.resume(null) {}
                    }
                }
            ) { _ -> if (continuation.isActive) continuation.resume(null) {} }
        } ?: return null

        raw.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(raw.avatar)
        raw.image = SupabaseStorageHelper.resolveMediaUrlAsync(raw.image)
        raw.video = SupabaseStorageHelper.resolveMediaUrlAsync(raw.video)
        return raw
    }

    override suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            FIRDatabase.database().reference()
                .child(newsPath)
                .child(new.id)
                .removeValueWithCompletionBlock { error, _ ->
                    if (continuation.isActive) continuation.resume(error == null)
                }
        }

    override suspend fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String) {
        if (value < 0) return
        FIRDatabase.database().reference()
            .child(newsPath)
            .child(newsId)
            .child(likedCountPath)
            .setValue(value.toLong()) { _, _ -> }
    }

    override suspend fun getAllGroups(
        userPath: String,
        groupPath: String,
        userId: String
    ): Set<GroupSummaryDTO> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath)
            .child(userId)
            .child(groupPath)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = mutableSetOf<GroupSummaryDTO>()
            if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val map = child.value as? Map<*, *> ?: continue
                    val id = map["id"] as? String ?: child.key ?: continue
                    val name = map["name"] as? String ?: ""
                    val avatar = map["avatar"] as? String ?: ""
                    val notificationOn = map["notificationOn"] as? Boolean ?: false
                    result.add(GroupSummaryDTO(id = id, name = name, avatar = avatar, notificationOn = notificationOn))
                }
            }
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(emptySet()) }
    }

    override suspend fun fetchGroupInfo(groupId: String, groupPath: String): GroupDTO? = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(groupPath).child(groupId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val map = snapshot.value as? Map<*, *>
                if (map != null) {
                    val group = GroupDTO(
                        id = map["id"] as? String ?: groupId,
                        name = map["name"] as? String ?: "",
                        avatar = map["avatar"] as? String ?: "",
                        password = map["password"] as? String ?: "",
                        description = map["description"] as? String ?: "",
                        createdDate = (map["createdDate"] as? Long) ?: 0L,
                        memberCount = (map["memberCount"] as? Long) ?: 0L
                    )
                    if (cont.isActive) cont.resume(group)
                } else {
                    if (cont.isActive) cont.resume(null)
                }
            } else {
                if (cont.isActive) cont.resume(null)
            }
        }) { _ -> if (cont.isActive) cont.resume(null) }
    }

    override suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(groupPath).child(groupId).child(notificationStatusPath)
        ref.setValue(newStatus) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(groupPath).child(groupId).child(membersPath)
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
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(HashMap()) }
    }

    override suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(groupPath).child(groupId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val map = snapshot.value as? Map<*, *>
                val summary = if (map != null) GroupSummaryDTO(
                    id = map["id"] as? String ?: groupId,
                    name = map["name"] as? String ?: "",
                    avatar = map["avatar"] as? String ?: "",
                    notificationOn = map["notificationOn"] as? Boolean ?: false
                ) else GroupSummaryDTO()
                if (cont.isActive) cont.resume(summary)
            } else {
                if (cont.isActive) cont.resume(GroupSummaryDTO())
            }
        }) { _ -> if (cont.isActive) cont.resume(GroupSummaryDTO()) }
    }

    override suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(groupPath).child(groupId).child(notificationStatusPath)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val value = snapshot?.value as? Boolean ?: false
            if (cont.isActive) cont.resume(value)
        }) { _ -> if (cont.isActive) cont.resume(false) }
    }

    override suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val groupSummaryMap = mapOf<Any?, Any?>(
            "id" to group.id, "name" to group.name, "avatar" to group.avatar
        )
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/${group.id}/$memberPath/${user.uid}" to "member",
            "$userPath/${user.uid}/$groupPath/${group.id}" to groupSummaryMap
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (error == null) {
                // Increment memberCount
                val countRef = dbRef.child(groupPath).child(group.id).child(memberCountPath)
                countRef.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                    val current = (snapshot?.value as? Long) ?: 0L
                    countRef.setValue(current + 1) { _, _ -> }
                }) { _ -> }
            }
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/${group.id}/$memberPath/${user.uid}" to null,
            "$userPath/${user.uid}/$groupPath/${group.id}" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (error == null) {
                val countRef = dbRef.child(groupPath).child(group.id).child(memberCountPath)
                countRef.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                    val current = (snapshot?.value as? Long) ?: 0L
                    val newCount = if (current > 0) current - 1 else 0
                    countRef.setValue(newCount) { _, _ -> }
                }) { _ -> }
            }
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/${group.id}" to null,
            "$userPath/${user.uid}/$groupPath/${group.id}" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(groupPath).child(group.id).child(memberPath).child(user.uid)
        ref.setValue(role) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun fetchRecommendGroups(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(groupPath)
        ref.queryOrderedByChild(memberCountPath).queryLimitedToLast(limit.toULong())
            .observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                val result = mutableListOf<GroupDTO>()
                if (snapshot != null && snapshot.exists()) {
                    val children = snapshot.children
                    while (true) {
                        val child = children.nextObject() as? FIRDataSnapshot ?: break
                        val map = child.value as? Map<*, *> ?: continue
                        result.add(GroupDTO(
                            id = map["id"] as? String ?: child.key ?: "",
                            name = map["name"] as? String ?: "",
                            avatar = map["avatar"] as? String ?: "",
                            memberCount = (map["memberCount"] as? Long) ?: 0L
                        ))
                    }
                }
                if (cont.isActive) cont.resume(result.sortedByDescending { it.memberCount })
            }) { _ -> if (cont.isActive) cont.resume(emptyList()) }
    }

    override suspend fun createPoll(
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
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val pollMap = mapOf<Any?, Any?>(
            "id" to poll.id,
            "posterId" to poll.posterId,
            "posterName" to poll.posterName,
            "posterAvatar" to poll.posterAvatar,
            "question" to poll.question,
            "options" to poll.options,
            "allowMultipleAnswers" to poll.allowMultipleAnswers,
            "duration" to poll.duration,
            "groupId" to poll.groupId,
            "likeCount" to poll.likeCount,
            "commentCount" to poll.commentCount,
            "timePosted" to poll.timePosted,
            "expiresAt" to poll.expiresAt,
            "votes" to (poll.votes ?: emptyMap<String, Int>())
        )
        val newsMap = mapOf<Any?, Any?>(
            "id" to newsId,
            "posterId" to newsPosterId,
            "posterName" to newsPosterName,
            "avatar" to newsAvatar,
            "message" to newsMessage,
            "timePosted" to newsTimePosted
        )
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to newsMap,
            "$pollPath/${poll.id}" to pollMap
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
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
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to null,
            "$pollPath/$pollId" to null,
            "$pollVotesPath/$pollId" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
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
                    if (cont.isActive) cont.resume(poll)
                } else {
                    if (cont.isActive) cont.resume(null)
                }
            } else {
                if (cont.isActive) cont.resume(null)
            }
        }) { _ -> if (cont.isActive) cont.resume(null) }
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
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(emptyList()) }
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
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(emptyMap()) }
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
                if (cont.isActive) cont.resume(error == null)
            }
        }) { _ -> if (cont.isActive) cont.resume(false) }
    }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return IosDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }
}
