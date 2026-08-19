package com.minhtu.firesocialmedia.data.repository.notification
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.notification.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationDatabaseService
import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository

class UserRepositoryImpl(
    private val authService: AuthSessionService,
    private val notificationDatabaseService: NotificationDatabaseService
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        return notificationDatabaseService.getUser(userId)
    }

    override suspend fun getCurrentUserUid(): String? = authService.getCurrentUserUid()
}
