package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.mapper.room.toDomain
import com.minhtu.firesocialmedia.data.local.mapper.room.toNewEntity
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.home.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDto
import com.minhtu.firesocialmedia.data.remote.mapper.settings.toDomain
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.core.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.core.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository
import kotlinx.coroutines.flow.first

class NewsRepositoryImpl(
    private val databaseService: DatabaseService,
    private val localDatabaseService : RoomService,
    private val networkMonitor: NetworkMonitor
) : NewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.getNew(newId)?.toDomain()
        } else {
            localDatabaseService.getNewById(newId)?.toDomain()
        }
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?
    ): LatestNewsResult? {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            val latestNewsResult = databaseService.getLatestNews(
                number,
                lastTimePosted,
                lastKey,
                DataConstant.NEWS_PATH).toDomain()
            if(latestNewsResult != null) {
                latestNewsResult.news?.let { newsList ->
                    localDatabaseService.storeNewsToRoom(newsList.toNewEntity())
                }
            }
            return latestNewsResult
        } else {
            val rows = if (lastTimePosted == null) {
                localDatabaseService.getFirstPage(number)
            } else {
                localDatabaseService.getPageAfter(number, lastTimePosted.toLong(), lastKey)
            }

            val itemsDomain = rows.map { it.toDomain() }
            val last = rows.lastOrNull()

            return LatestNewsResult(
                news = itemsDomain,
                lastTimePostedValue = last?.timePosted?.toDouble(),
                lastKeyValue = last?.id
            )
        }
    }

    override suspend fun deleteNewsFromDatabase(
        new: NewsInstance
    ) {
        databaseService.deleteNewsFromDatabase(DataConstant.NEWS_PATH, new.toDto())
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupId: String
    ): Boolean {
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

    override suspend fun updateNewsFromDatabase(
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsInstance
    ): Boolean {
        return databaseService.updateNewsFromDatabase(
            DataConstant.NEWS_PATH,
            newContent,
            newImage,
            newVideo,
            new.toDto())
    }

    override suspend fun fetchPoll(pollId: String): PollObject? {
        return databaseService.fetchPoll(pollId, DataConstant.POLL_PATH)?.toDomain()
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
}