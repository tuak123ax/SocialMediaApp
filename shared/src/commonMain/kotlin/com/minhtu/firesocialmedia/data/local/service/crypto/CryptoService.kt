package com.minhtu.firesocialmedia.data.local.service.crypto

import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO
import io.mockative.Mockable

@Mockable
interface CryptoService {
    fun saveAccount(email: String, password: String)
    suspend fun loadAccount(): CredentialsDTO?
    suspend fun clearAccount()
    suspend fun getFCMToken() : String
}