package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.mapper.user.toDomain
import com.minhtu.firesocialmedia.data.mapper.user.toDto
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.AuthService

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
        name: String,
        path: String
    ): List<UserInstance>? {
        return databaseService.searchUserByName(
            name,
            path
        )?.map { it.toDomain() }
    }
}