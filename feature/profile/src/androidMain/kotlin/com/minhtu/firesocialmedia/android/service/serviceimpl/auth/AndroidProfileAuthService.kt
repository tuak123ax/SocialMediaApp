package com.minhtu.firesocialmedia.android.service.serviceimpl.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService
import kotlinx.coroutines.tasks.await

class AndroidProfileAuthService : ProfileAuthService {
    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return false

        val credential = EmailAuthProvider.getCredential(currentUserEmail, currentPassword)

        return try {
            user.reauthenticate(credential).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
