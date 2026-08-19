package com.minhtu.firesocialmedia.data.repository.news

import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.network.profile.NetworkMonitor
import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.news.ProfileNewsPage
import kotlinx.coroutines.flow.first

private fun NewsDTO.toProfileDomain(): NewsInstance = NewsInstance(
    id = id, posterId = posterId, posterName = posterName, avatar = avatar,
    message = message, image = image, video = video, isVisible = isVisible,
    likeCount = likeCount, commentCount = commentCount, timePosted = timePosted,
    localPath = localPath, shareContentId = shareContentId,
    decentralizationType = when (decentralizationType) {
        "Public" -> DecentralizationType.Public
        "Private" -> DecentralizationType.Private
        "OnlyFriends" -> DecentralizationType.OnlyFriends
        else -> null
    },
    groupId = groupId, type = type, pollId = pollId
)

private fun NewsInstance.toProfileDto(): NewsDTO = NewsDTO(
    id = id, posterId = posterId, posterName = posterName, avatar = avatar,
    message = message, image = image, video = video, isVisible = isVisible,
    likeCount = likeCount, commentCount = commentCount, timePosted = timePosted,
    localPath = localPath, shareContentId = shareContentId,
    decentralizationType = decentralizationType?.toString() ?: "",
    groupId = groupId, type = type, pollId = pollId
)

class ProfileNewsRepositoryImpl(
    private val databaseService: ProfileDatabaseService,
    private val networkMonitor: NetworkMonitor
) : ProfileNewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? {
        val isOnline = networkMonitor.isOnline.first()
        return if (isOnline) {
            databaseService.getNew(newId, DataConstant.NEWS_PATH)?.toProfileDomain()
        } else null
    }

    override suspend fun getNewsByUser(
        posterId: String,
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?
    ): ProfileNewsPage {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return ProfileNewsPage(emptyList(), lastTimePosted, lastKey)

        val result = databaseService.getNewsByPoster(posterId, number, lastTimePosted, lastKey, DataConstant.NEWS_PATH)
        return ProfileNewsPage(
            news = result.news.map { it.toProfileDomain() },
            lastTimePosted = result.lastTimePostedValue,
            lastKey = result.lastKeyValue
        )
    }

    override suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        return if (isOnline) {
            databaseService.deleteNewsFromDatabase(new.toProfileDto(), DataConstant.NEWS_PATH)
        } else false
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
}
