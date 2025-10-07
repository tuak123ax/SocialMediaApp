package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDto
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService

class UserRepositoryImpl(
    private val authService: AuthService,
    private val databaseService: DatabaseService
) : UserRepository {
    override suspend fun getUser(userId: String): UserInstance? {
        return databaseService.getUser(userId)?.toDomain()
    }

    override suspend fun getCurrentUserUid(): String? {
        return authService.getCurrentUserUid()
    }

    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {
        databaseService.updateFCMTokenForCurrentUser(user.toDto())
    }

    override suspend fun searchUserByName(
        name: String
    ): List<UserInstance>? {
        return databaseService.searchUserByName(
            name,
            DataConstant.USER_PATH
        ).orEmpty().map{it.toDomain()}
    }
}