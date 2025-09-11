package com.minhtu.firesocialmedia.domain.usecases.common

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveListToDatabaseUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                path : String,
                                value : ArrayList<String>,
                                externalPath : String) {
        return commonDbRepository.saveListToDatabase(id, path, value, externalPath)
    }
}