package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService

class CommonDbRepositoryImpl(
    private val databaseService: DatabaseService
) : CommonDbRepository {
    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return databaseService.saveValueToDatabase(
            id,
            path,
            value,
            externalPath
        )
    }

    override suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    ) {
        databaseService.saveListToDatabase(
            id,
            path,
            value,
            externalPath
        )
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            path,
            externalPath,
            value
        )
    }

    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance) : Boolean {
        return databaseService.saveInstanceToDatabase(
            id,
            path,
            instance
        )
    }

    override suspend fun deleteInstanceFromDatabase(
        path: String,
        instance: BaseNewsInstance
    ) {
        databaseService.deleteCommentFromDatabase(
            path,
            instance
        )
    }
}