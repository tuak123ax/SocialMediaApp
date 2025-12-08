package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.usecases.home.DeleteNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetLatestNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreNewsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SaveNewToDatabaseUseCase

class NewsInteractorImpl(
    private val getLatestNewsUseCase: GetLatestNewsUseCase,
    private val updateLikeCountForNewUseCase : UpdateLikeCountForNewUseCase,
    private val deleteNewsFromDatabaseUseCase: DeleteNewsFromDatabaseUseCase,
    private val storeNewsToRoomUseCase: StoreNewsToRoomUseCase,
    private val saveNewToDatabase : SaveNewToDatabaseUseCase,
) : NewsInteractor {
    override suspend fun pageLatest(
        number : Int,
        lastTimePosted : Double?,
        lastKey: String?
    ): LatestNewsResult? {
        return getLatestNewsUseCase.invoke(
            10,
            lastTimePosted,
            lastKey
        )
    }

    override suspend fun like(
        id: String,
        value: Int
    ) {
        updateLikeCountForNewUseCase.invoke(
            id,
            value
        )
    }

    override suspend fun unlike(
        id: String,
        value: Int
    ) {
        updateLikeCountForNewUseCase.invoke(
            id,
            value
        )
    }

    override suspend fun delete(
        new: NewsInstance
    ) {
        deleteNewsFromDatabaseUseCase.invoke(new)
    }

    override suspend fun storeNewsToRoom(news: List<NewsInstance>) {
        storeNewsToRoomUseCase.invoke(news)
    }

    override suspend fun saveNews(news: NewsInstance) : Boolean {
        return saveNewToDatabase.invoke(
            news
        )
    }
}