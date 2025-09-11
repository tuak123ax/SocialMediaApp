package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveNewToDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(
        id : String,
        path : String,
        instance : BaseNewsInstance) : Boolean {
        return commonDbRepository.saveInstanceToDatabase(
            id,
            path,
            instance
        )
    }
}