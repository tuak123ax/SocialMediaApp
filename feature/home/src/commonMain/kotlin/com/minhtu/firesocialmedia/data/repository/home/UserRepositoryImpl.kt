package com.minhtu.firesocialmedia.data.repository.home
import com.minhtu.firesocialmedia.constants.home.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.auth.home.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService
import com.minhtu.firesocialmedia.domain.repository.home.UserRepository
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO

class UserRepositoryImpl(
    private val authService: AuthSessionService,
    private val homeDatabaseService: HomeDatabaseService
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        return homeDatabaseService.getUser(userId)
    }
    override suspend fun getCurrentUserUid(): String? = authService.getCurrentUserUid()
    override suspend fun searchUserByName(name: String): List<UserDTO>? {
        return homeDatabaseService.searchUserByName(name, DataConstant.USER_PATH)
    }
    override suspend fun updateFCMTokenForCurrentUser(user: UserDTO) {
        homeDatabaseService.updateFCMTokenForCurrentUser(user)
    }
}
