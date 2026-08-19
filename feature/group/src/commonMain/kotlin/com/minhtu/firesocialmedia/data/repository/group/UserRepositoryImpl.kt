package com.minhtu.firesocialmedia.data.repository.group
import com.minhtu.firesocialmedia.network.group.NetworkMonitor
import com.minhtu.firesocialmedia.constants.group.DataConstant
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.group.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.GroupDatabaseService
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository

class UserRepositoryImpl(
    private val authService: AuthSessionService,
    private val groupDatabaseService: GroupDatabaseService,
    private val networkMonitor: NetworkMonitor
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        return groupDatabaseService.getUser(userId)
    }

    override suspend fun getCurrentUserUid(): String? {
        return authService.getCurrentUserUid()
    }

    override suspend fun saveLikedPost(
        userId: String,
        likedPosts: HashMap<String, Int>
    ): Boolean {
        return groupDatabaseService.saveValueToDatabase(
            userId,
            DataConstant.USER_PATH,
            likedPosts,
            DataConstant.LIKED_POSTS_PATH
        )
    }
}
