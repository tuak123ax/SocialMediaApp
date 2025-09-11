package com.minhtu.firesocialmedia.domain.usecases.common

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveValueToDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                path : String,
                                value : HashMap<String, Int>,
                                externalPath : String) : Boolean {
        return commonDbRepository.saveValueToDatabase(id, path, value, externalPath)
    }
}