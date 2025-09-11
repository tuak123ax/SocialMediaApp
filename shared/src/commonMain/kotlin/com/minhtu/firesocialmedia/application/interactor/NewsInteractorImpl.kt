package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.usecases.home.DeleteNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetLatestNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.UpdateCountValueInDatabase

class NewsInteractorImpl(
    private val getLatestNewsUseCase: GetLatestNewsUseCase,
    private val updateCountValueInDatabaseUseCase : UpdateCountValueInDatabase,
    private val deleteNewsFromDatabaseUseCase: DeleteNewsFromDatabaseUseCase
) : NewsInteractor {
    override suspend fun pageLatest(
        number : Int,
        lastTimePosted : Double?,
        lastKey: String?,
        path: String
    ): LatestNewsResult? {
        return getLatestNewsUseCase.invoke(
            10,
            lastTimePosted,
            lastKey,
            Constants.NEWS_PATH
        )
    }

    override suspend fun like(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        updateCountValueInDatabaseUseCase.invoke(
            id,
            path,
            externalPath,
            value
        )
    }

    override suspend fun unlike(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        updateCountValueInDatabaseUseCase.invoke(
            id,
            path,
            externalPath,
            value
        )
    }

    override suspend fun delete(
        path: String,
        new: NewsInstance
    ) {
        deleteNewsFromDatabaseUseCase.invoke(path, new)
    }
}