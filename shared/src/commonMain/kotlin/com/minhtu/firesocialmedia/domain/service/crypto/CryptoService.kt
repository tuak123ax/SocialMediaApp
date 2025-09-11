package com.minhtu.firesocialmedia.domain.service.crypto

import com.minhtu.firesocialmedia.data.dto.crypto.CredentialsDTO
import io.mockative.Mockable

@Mockable
interface CryptoService {
    fun saveAccount(email: String, password: String)
    suspend fun loadAccount(): CredentialsDTO?
    suspend fun clearAccount()
    suspend fun getFCMToken() : String
}