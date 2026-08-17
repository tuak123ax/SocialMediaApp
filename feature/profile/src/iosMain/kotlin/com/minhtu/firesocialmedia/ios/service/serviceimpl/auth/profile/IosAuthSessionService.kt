package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.profile

import cocoapods.FirebaseAuth.FIRAuth
import com.minhtu.firesocialmedia.data.remote.service.auth.profile.AuthSessionService

class IosAuthSessionService : AuthSessionService {
    override suspend fun getCurrentUserUid(): String? {
        return FIRAuth.auth().currentUser()?.uid()
    }

    override suspend fun getCurrentUserEmail(): String? {
        return FIRAuth.auth().currentUser()?.email()
    }
}
