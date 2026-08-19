package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIREmailAuthProvider
import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService
import kotlinx.coroutines.suspendCancellableCoroutine

class IosProfileAuthService : ProfileAuthService {
    override suspend fun reAuthenticate(
        currentUserEmail: String,
        currentPassword: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val user = FIRAuth.auth().currentUser() ?: run {
            cont.resume(false) {}
            return@suspendCancellableCoroutine
        }
        val credential = FIREmailAuthProvider.credentialWithEmail(
            currentUserEmail,
            password = currentPassword
        )
        user.reauthenticateWithCredential(credential) { _, error ->
            if (cont.isActive) cont.resume(error == null) {}
        }
    }
}
