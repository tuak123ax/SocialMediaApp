package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface SettingsRepository {
    suspend fun changePassword(user : UserInstance,
                               newPassword : String) : ChangePasswordState
    suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String) : Boolean
}