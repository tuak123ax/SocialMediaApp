package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.remote.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO

/**
 * Feature-home-owned counterpart of core's generic [DatabaseService], scoped to News-specific
 * read/write operations only (Firebase Realtime Database + Supabase Storage for media). This
 * mirrors feature/group's `GroupDatabaseService` pattern: keeps core's `DatabaseService` free of
 * any News-typed methods while feature/home retains direct access to the underlying data source.
 */
interface HomeDatabaseService {
    suspend fun getNew(newId: String): NewsDTO?

    suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsDTO

    suspend fun deleteNewsFromDatabase(
        path: String,
        new: NewsDTO
    )

    suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean

    suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean

    suspend fun saveNewToGroup(groupId: String, instance: NewsDTO): Boolean

    suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String>

    suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean

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

    suspend fun searchUserByName(name: String, path: String): List<UserDTO>?

    suspend fun updateFCMTokenForCurrentUser(currentUser: UserDTO)

    suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean

    suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    )
}
