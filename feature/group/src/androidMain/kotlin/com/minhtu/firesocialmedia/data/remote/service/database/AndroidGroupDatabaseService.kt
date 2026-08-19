package com.minhtu.firesocialmedia.data.remote.service.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.group.AndroidDatabaseHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.constants.group.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidGroupDatabaseService : GroupDatabaseService {
    override suspend fun getNew(newId: String, newsPath: String): NewsDTO? {
        return AndroidGroupDatabaseHelper.getNew(newId, newsPath)
    }

    override suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean {
        return AndroidGroupDatabaseHelper.deleteNewsFromDatabase(new, newsPath)
    }

    override suspend fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String) {
        AndroidGroupDatabaseHelper.updateLikeCountForNew(newsId, value, newsPath, likedCountPath)
    }

    override suspend fun getAllGroups(
        userPath: String,
        groupPath: String,
        userId: String
    ): Set<GroupSummaryDTO> {
        return AndroidGroupDatabaseHelper.getAllGroups(userPath, groupPath, userId)
    }

    override suspend fun fetchGroupInfo(
        groupId: String,
        groupPath: String
    ): GroupDTO? {
        return AndroidGroupDatabaseHelper.fetchGroupInfo(groupId, groupPath)
    }

    override suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.updateNotificationStatus(
            newStatus,
            groupId,
            userId,
            userPath,
            groupPath,
            notificationStatusPath
        )
    }

    override suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String> {
        return AndroidGroupDatabaseHelper.getAllMembersInGroup(
            groupId,
            groupPath,
            membersPath
        )
    }

    override suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO {
        return AndroidGroupDatabaseHelper.getGroupConfigs(
            userId,
            groupId,
            userPath,
            groupPath
        )
    }

    override suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.fetchNotificationState(
            userId,
            groupId,
            userPath,
            groupPath,
            notificationStatusPath
        )
    }

    override suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.addUserToGroup(
            user,
            group,
            userPath,
            groupPath,
            memberPath,
            memberCountPath
        )
    }

    override suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.removeUserFromGroup(
            user,
            group,
            userPath,
            groupPath,
            memberPath,
            memberCountPath
        )
    }

    override suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.deleteGroup(
            user,
            group,
            userPath,
            groupPath
        )
    }

    override suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.updateMemberRole(
            role,
            user,
            group,
            groupPath,
            memberPath
        )
    }

    override suspend fun fetchRecommendGroups(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO> {
        val raw = AndroidGroupDatabaseHelper.fetchGroupsByMemberCount(limit, groupPath, memberCountPath)
        return coroutineScope {
            raw.map { group -> async { group.copy(avatar = resolveMediaUrlAsync(group.avatar)) } }.awaitAll()
        }
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
    ): Boolean {
        return AndroidGroupDatabaseHelper.createPoll(
            poll, pollPath, groupPath, groupId, postsPath,
            newsId, newsPosterId, newsPosterName, newsAvatar, newsMessage,
            newsLikeCount, newsCommentCount, newsTimePosted, newsType, newsPollId
        )
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        pollPath: String,
        pollVotesPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.deletePollFromDatabase(newsId, pollId, groupPath, groupId, postsPath, pollPath, pollVotesPath)
    }

    override suspend fun fetchPoll(pollId: String, pollPath: String): PollDTO? {
        return AndroidGroupDatabaseHelper.fetchPoll(pollId, pollPath)
    }

    override suspend fun loadMyVotes(pollId: String, userId: String, pollVotesPath: String): List<Int> {
        return AndroidGroupDatabaseHelper.loadMyVotes(pollId, userId, pollVotesPath)
    }

    override suspend fun loadAllVoters(pollId: String, pollVotesPath: String): Map<String, List<Int>> {
        return AndroidGroupDatabaseHelper.loadAllVoters(pollId, pollVotesPath)
    }

    override suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>,
        pollPath: String,
        pollVotesPath: String
    ): Boolean {
        return AndroidGroupDatabaseHelper.submitVote(pollId, userId, selectedIndices, previousIndices, pollPath, pollVotesPath)
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

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return AndroidDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }
}
