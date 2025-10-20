package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.mapper.room.toDomain
import com.minhtu.firesocialmedia.data.local.mapper.room.toRoomEntity
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDto
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val authService: AuthService,
    private val databaseService: DatabaseService,
    private val cryptoService: CryptoService,
    private val localDatabaseService : RoomService,
    private val networkMonitor: NetworkMonitor
) : UserRepository {
    override suspend fun getUser(userId: String,
                                 isCurrentUser: Boolean): UserInstance? {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            val user = databaseService.getUser(userId)?.toDomain()
            if(user != null && !isCurrentUser) {
                localDatabaseService.storeUserFriendToRoom(user.toRoomEntity())
            }
            return user
        } else {
            return if(isCurrentUser) {
                cryptoService.getCurrentUserInfo()?.toDomain()
            } else {
                localDatabaseService.getUserFromRoom(userId)?.toDomain()
            }
        }
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