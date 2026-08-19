package com.minhtu.firesocialmedia.data.repository.profile
import com.minhtu.firesocialmedia.data.local.service.crypto.ProfileCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.LikedPostRoomService
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService
import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.profile.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.network.profile.NetworkMonitor
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val authService: AuthSessionService,
    private val profileDatabaseService: ProfileDatabaseService,
    private val cryptoService: ProfileCryptoService,
    private val userRoomService : UserRoomService,
    private val newsRoomService : LikedPostRoomService,
    private val networkMonitor: NetworkMonitor
) : UserRepository {
    override suspend fun getUser(userId: String,
                                 isCurrentUser: Boolean): UserDTO? {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            val user = profileDatabaseService.getUser(userId)
            if(user != null && !isCurrentUser) {
                userRoomService.storeUserFriendToRoom(user)
            }
            return user
        } else {
            return if(isCurrentUser) {
                cryptoService.getCurrentUserInfo()
            } else {
                userRoomService.getUserFromRoom(userId)
            }
        }
    }

    override suspend fun getCurrentUserUid(): String? {
        return authService.getCurrentUserUid()
    }

    override suspend fun saveLikedPost(
        userId: String,
        likedPosts: HashMap<String, Int>
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            return profileDatabaseService.saveValueToDatabase(
                userId,
                DataConstant.USER_PATH,
                likedPosts,
                DataConstant.LIKED_POSTS_PATH
            )
        } else {
            try {
                newsRoomService.saveLikedPost(
                    likedPosts
                )
                return true
            } catch (ex: Exception) {
                logMessage("saveLikedPost", { "Exception when saveLikedPost: ${ex.message}" })
                return false
            }
        }
    }
}
