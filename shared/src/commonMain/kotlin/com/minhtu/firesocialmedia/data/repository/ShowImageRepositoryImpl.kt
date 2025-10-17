package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.repository.ShowImageRepository

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