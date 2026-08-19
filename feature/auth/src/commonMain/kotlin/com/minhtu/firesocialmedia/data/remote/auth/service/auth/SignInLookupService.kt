package com.minhtu.firesocialmedia.data.remote.auth.service.auth

import com.minhtu.firesocialmedia.data.remote.auth.dto.signin.SignInDTO


/**
 * Looks up whether an account already exists for a given email.
 * Extracted from core's generic DatabaseService — feature/auth is its only owner.
 */
interface SignInLookupService {
    suspend fun checkUserExists(email: String): SignInDTO
}
