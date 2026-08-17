package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.home.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDto
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService
import com.minhtu.firesocialmedia.network.home.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import kotlinx.coroutines.flow.first

/**
 * All News- and Poll-typed reads/writes, plus generic count updates, go through feature/home's
 * own [homeDatabaseService] ([HomeDatabaseService]), so core no longer needs to know about News
 * or Poll at all.
 */
class NewsRepositoryImpl(
    private val homeDatabaseService: HomeDatabaseService,
    private val localDatabaseService : HomeNewsRoomService,
    private val networkMonitor: NetworkMonitor
) : NewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            homeDatabaseService.getNew(newId)?.toDomain()
        } else {
            localDatabaseService.getNewById(newId)
        }
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?
    ): LatestNewsResult? {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            val latestNewsResult = homeDatabaseService.getLatestNews(
                number,
                lastTimePosted,
                lastKey,
                DataConstant.NEWS_PATH).toDomain()
            if(latestNewsResult != null) {
                latestNewsResult.news?.let { newsList ->
                    localDatabaseService.storeNewsToRoom(newsList)
                }
            }
            return latestNewsResult
        } else {
            val rows = if (lastTimePosted == null) {
                localDatabaseService.getFirstPage(number)
            } else {
                localDatabaseService.getPageAfter(number, lastTimePosted.toLong(), lastKey)
            }

            val last = rows.lastOrNull()

            return LatestNewsResult(
                news = rows,
                lastTimePostedValue = last?.timePosted?.toDouble(),
                lastKeyValue = last?.id
            )
        }
    }

    override suspend fun updateLikeCountForNew(newsId: String, value: Int) {
        homeDatabaseService.updateCountValueInDatabase(
            newsId,
            DataConstant.NEWS_PATH,
            DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun deleteNewsFromDatabase(
        new: NewsInstance
    ) {
        homeDatabaseService.deleteNewsFromDatabase(DataConstant.NEWS_PATH, new.toDto())
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupId: String
    ): Boolean {
        return homeDatabaseService.deletePollFromDatabase(
            newsId,
            pollId,
            DataConstant.GROUP_PATH,
            groupId,
            DataConstant.POSTS_PATH,
            DataConstant.POLL_PATH,
            DataConstant.POLL_VOTES_PATH
        )
    }

    override suspend fun updateNewsFromDatabase(
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsInstance
    ): Boolean {
        return homeDatabaseService.updateNewsFromDatabase(
            DataConstant.NEWS_PATH,
            newContent,
            newImage,
            newVideo,
            new.toDto())
    }

    override suspend fun fetchPoll(pollId: String): PollDTO? {
        return homeDatabaseService.fetchPoll(pollId, DataConstant.POLL_PATH)
    }

    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> {
        return homeDatabaseService.loadMyVotes(pollId, userId, DataConstant.POLL_VOTES_PATH)
    }

    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> {
        return homeDatabaseService.loadAllVoters(pollId, DataConstant.POLL_VOTES_PATH)
    }

    override suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>
    ): Boolean {
        return homeDatabaseService.submitVote(
            pollId, userId, selectedIndices, previousIndices,
            DataConstant.POLL_PATH, DataConstant.POLL_VOTES_PATH
        )
    }
}
