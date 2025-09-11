package com.minhtu.firesocialmedia.domain.usecases.common

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class UpdateCountValueInDatabase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id: String,
                                path: String,
                                externalPath: String,
                                value: Int) {
        commonDbRepository.updateCountValueInDatabase(id, path, externalPath, value)
    }
}