package com.minhtu.firesocialmedia.data.repository.appinit
import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.constants.search.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.auth.appinit.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.AppInitDatabaseService
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository

class UserRepositoryImpl(
    private val authService: AuthSessionService,
    private val appInitDatabaseService: AppInitDatabaseService
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        return appInitDatabaseService.getUser(userId)
    }

    override suspend fun getCurrentUserUid(): String? = authService.getCurrentUserUid()

    override suspend fun searchUserByName(name: String): List<UserDTO>? {
        return appInitDatabaseService.searchUserByName(name, DataConstant.USER_PATH)
    }
}
