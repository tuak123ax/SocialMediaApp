package com.minhtu.firesocialmedia.data.local.service.crypto

import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import io.mockative.Mockable

@Mockable
interface CryptoService {
    fun saveAccount(email: String, password: String)
    suspend fun loadAccount(): CredentialsDTO?
    suspend fun clearAccount()
    suspend fun getFCMToken() : String
    suspend fun saveCurrentUserInfo(user: UserDTO)
    suspend fun getCurrentUserInfo() : UserDTO?

}