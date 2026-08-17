package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.entity.user.auth.toDto
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.mapper.auth.toEmailExistResult
import com.minhtu.firesocialmedia.data.remote.mapper.auth.toSignInError
import com.minhtu.firesocialmedia.data.remote.mapper.auth.toSignUpError
import com.minhtu.firesocialmedia.data.remote.mapper.crypto.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.signin.toDomain
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.SignInLookupService
import com.minhtu.firesocialmedia.data.remote.service.database.AuthDatabaseService

class AuthenticationRepositoryImpl(
    private val authService: AuthService,
    private val authDatabaseService: AuthDatabaseService,
    private val signInLookupService: SignInLookupService,
    private val cryptoService: AuthCryptoService
) : AuthenticationRepository {
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): SignInError? {
        return authService.signInWithEmailAndPassword(email, password)
            .exceptionOrNull()?.toSignInError()
    }

    override suspend fun saveAccountToLocalStorage(email: String, password: String) {
        cryptoService.saveAccount(email, password)
    }

    override suspend fun checkUserExists(email: String): SignInState {
        return signInLookupService.checkUserExists(email).toDomain()
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
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it.toSignUpError()) }
            )
    }

    override suspend fun fetchSignInMethodsForEmail(email: String): EmailExistResult {
        return authService.fetchSignInMethodsForEmail(email).toEmailExistResult()
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        return authService.sendPasswordResetEmail(email)
    }

    override suspend fun clearAccount() {
        cryptoService.clearAccount()
    }

    override suspend fun saveSignUpInformation(userInstance: UserInstance): Boolean {
        return authDatabaseService.saveSignUpInformation(userInstance.toDto())
    }
}