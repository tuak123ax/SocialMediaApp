package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.domain.repository.ShowImageRepository
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService

class ShowImageRepositoryImpl(
    private val databaseService: DatabaseService
) : ShowImageRepository {
    override suspend fun downloadImage(
        image: String,
        fileName: String
    ): Boolean {
        return databaseService.downloadImage(image, fileName)
    }
}