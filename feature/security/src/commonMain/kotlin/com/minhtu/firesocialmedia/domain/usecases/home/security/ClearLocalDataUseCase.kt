package com.minhtu.firesocialmedia.domain.usecases.home.security

class ClearLocalDataUseCase(
    private val cleaners: List<LocalDataCleaner>
) {
    suspend operator fun invoke() {
        cleaners.forEach { it.clear() }
    }
}

