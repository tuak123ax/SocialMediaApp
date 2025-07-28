package com.minhtu.firesocialmedia.domain

import com.minhtu.firesocialmedia.data.model.crypto.Credentials
import io.mockative.Mockable

@Mockable
interface CryptoService {
    fun saveAccount(email: String, password: String)
    suspend fun loadAccount(): Credentials?
    fun clearAccount()
    suspend fun getFCMToken() : String
}