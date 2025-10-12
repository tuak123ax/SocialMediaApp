package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.mapper.crypto.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.signin.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDto
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.local.service.crypto.CryptoService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.error.signin.SignInError

class AuthenticationRepositoryImpl(
    private val authService: AuthService,
    private val databaseService: DatabaseService,
    private val cryptoService: CryptoService
) : AuthenticationRepository {
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): SignInError? {
        return authService.signInWithEmailAndPassword(email, password)
    }

    override suspend fun saveAccountToLocalStorage(email: String, password: String) {
        cryptoService.saveAccount(email, password)
    }

    override suspend fun checkUserExists(email: String): SignInState {
        return databaseService.checkUserExists(email).toDomain()
    }

    override suspend fun checkLocalAccount(): Credentials? {
        return cryptoService.loadAccount()?.toDomain()
    }

    override suspend fun handleSignInGoogleResult(credential: Any): String? {
        return authService.handleSignInGoogleResult(credential)
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return authService.signUpWithEmailAndPassword(email, password)
    }

    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult {
        return authService.fetchSignInMethodsForEmail(email)
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        return authService.sendPasswordResetEmail(email)
    }

    override suspend fun clearAccount() {
        cryptoService.clearAccount()
    }

    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean {
        return databaseService.saveSignUpInformation(userInstance.toDto())
    }
}