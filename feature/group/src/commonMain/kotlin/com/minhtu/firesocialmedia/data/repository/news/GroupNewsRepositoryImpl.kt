package com.minhtu.firesocialmedia.data.repository.news

import com.minhtu.firesocialmedia.constants.group.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.data.remote.service.database.GroupDatabaseService
import com.minhtu.firesocialmedia.network.group.NetworkMonitor
import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.group.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import kotlinx.coroutines.flow.first

private fun NewsDTO.toGroupDomain(): NewsInstance = NewsInstance(
    id = id,
    posterId = posterId,
    posterName = posterName,
    avatar = avatar,
    message = message,
    image = image,
    video = video,
    isVisible = isVisible,
    likeCount = likeCount,
    commentCount = commentCount,
    timePosted = timePosted,
    localPath = localPath,
    shareContentId = shareContentId,
    decentralizationType = when (decentralizationType) {
        "Public" -> DecentralizationType.Public
        "Private" -> DecentralizationType.Private
        "OnlyFriends" -> DecentralizationType.OnlyFriends
        else -> null
    },
    type = type,
    pollId = pollId
)

private fun NewsInstance.toGroupDto(): NewsDTO = NewsDTO(
    id = id,
    posterId = posterId,
    posterName = posterName,
    avatar = avatar,
    message = message,
    image = image,
    video = video,
    isVisible = isVisible,
    likeCount = likeCount,
    commentCount = commentCount,
    timePosted = timePosted,
    localPath = localPath,
    shareContentId = shareContentId,
    decentralizationType = decentralizationType?.toString() ?: "",
    type = type,
    pollId = pollId
)

class GroupNewsRepositoryImpl(
    private val databaseService: GroupDatabaseService,
    private val networkMonitor: NetworkMonitor
) : GroupNewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? {
        val isOnline = networkMonitor.isOnline.first()
        return if (isOnline) {
            databaseService.getNew(newId, DataConstant.NEWS_PATH)?.toGroupDomain()
        } else {
            null
        }
    }

    override suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        return if (isOnline) {
            databaseService.deleteNewsFromDatabase(new.toGroupDto(), DataConstant.NEWS_PATH)
        } else {
            false
        }
    }

    override suspend fun updateLikeCountForNew(newsId: String, value: Int) {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            databaseService.updateLikeCountForNew(newsId, value, DataConstant.NEWS_PATH, DataConstant.LIKED_COUNT_PATH)
        }
    }

    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean {
        return databaseService.deletePollFromDatabase(
            newsId,
            pollId,
            DataConstant.GROUP_PATH,
            groupId,
            DataConstant.POSTS_PATH,
            DataConstant.POLL_PATH,
            DataConstant.POLL_VOTES_PATH
        )
    }

    override suspend fun fetchPoll(pollId: String): PollDTO? {
        return databaseService.fetchPoll(pollId, DataConstant.POLL_PATH)
    }

    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> {
        return databaseService.loadMyVotes(pollId, userId, DataConstant.POLL_VOTES_PATH)
    }

    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> {
        return databaseService.loadAllVoters(pollId, DataConstant.POLL_VOTES_PATH)
    }

    override suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>
    ): Boolean {
        return databaseService.submitVote(
            pollId, userId, selectedIndices, previousIndices,
            DataConstant.POLL_PATH, DataConstant.POLL_VOTES_PATH
        )
    }

    override suspend fun createPoll(poll: PollDTO, newsId: String, groupId: String): Boolean {
        return databaseService.createPoll(
            poll,
            DataConstant.POLL_PATH,
            DataConstant.GROUP_PATH,
            groupId,
            DataConstant.POSTS_PATH,
            newsId = newsId,
            newsPosterId = poll.posterId,
            newsPosterName = poll.posterName,
            newsAvatar = poll.posterAvatar,
            newsMessage = poll.question,
            newsLikeCount = poll.likeCount,
            newsCommentCount = poll.commentCount,
            newsTimePosted = poll.timePosted,
            newsType = DataConstant.POST_TYPE_POLL,
            newsPollId = poll.id
        )
    }
}
