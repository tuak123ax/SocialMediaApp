package com.minhtu.firesocialmedia.data.remote.auth.service.database

import com.minhtu.firesocialmedia.data.remote.auth.dto.settings.auth.IpInfoResponseDTO
import com.minhtu.firesocialmedia.data.remote.auth.dto.user.UserDTO

interface AuthDatabaseService {
    suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    )

    suspend fun getUser(userId: String): UserDTO?

    suspend fun saveSignUpInformation(user: UserDTO): Boolean
}
