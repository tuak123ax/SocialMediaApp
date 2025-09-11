package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance

interface CommonDbRepository {
    suspend fun saveValueToDatabase(id : String,
                                    path : String,
                                    value : HashMap<String, Int>,
                                    externalPath : String) : Boolean
    suspend fun saveListToDatabase(
        id : String,
        path : String,
        value : ArrayList<String>,
        externalPath : String
    )
    suspend fun updateCountValueInDatabase(id: String,
                                           path: String,
                                           externalPath: String,
                                           value: Int)

    suspend fun saveInstanceToDatabase(
        id : String,
        path : String,
        instance : BaseNewsInstance) : Boolean

    suspend fun deleteInstanceFromDatabase(
        path : String,
        instance : BaseNewsInstance)
}