package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO

interface GroupDatabaseService {
    suspend fun getNew(newId: String, newsPath: String): NewsDTO?

    suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean

    suspend fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String)
    suspend fun getAllGroups(
        userPath: String,
        groupPath: String,
        userId: String
    ): Set<GroupSummaryDTO>

    suspend fun fetchGroupInfo(groupId: String, groupPath: String): GroupDTO?

    suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean

    suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String>

    suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO

    suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean

    suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean

    suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean

    suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean

    suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean

    suspend fun fetchRecommendGroups(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO>

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
    ): Boolean

    suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupPath: String, groupId: String, postsPath: String, pollPath: String, pollVotesPath: String): Boolean

    /**
     * Fetches the full [PollDTO] from /polls/{pollId}.
     * Returns null if the poll does not exist.
     */
    suspend fun fetchPoll(pollId: String, pollPath: String): PollDTO?

    /**
     * Loads the current user's selected option indices from /pollVotes/{pollId}/{userId}.
     * Returns an empty list if the user has not voted yet.
     */
    suspend fun loadMyVotes(pollId: String, userId: String, pollVotesPath: String): List<Int>

    /**
     * Loads all voters for a poll from /pollVotes/{pollId}.
     * Returns a map of userId -> list of selected option indices.
     */
    suspend fun loadAllVoters(pollId: String, pollVotesPath: String): Map<String, List<Int>>

    /**
     * Atomically casts a vote:
     *  - writes selected indices to /pollVotes/{pollId}/{userId}
     *  - increments /polls/{pollId}/votes/{optionIndex} for each selected option
     *  - removes previous vote increments if [previousIndices] is provided
     */
    suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>,
        pollPath: String,
        pollVotesPath: String
    ): Boolean

    suspend fun getUser(userId: String): UserDTO?

    suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean
}
