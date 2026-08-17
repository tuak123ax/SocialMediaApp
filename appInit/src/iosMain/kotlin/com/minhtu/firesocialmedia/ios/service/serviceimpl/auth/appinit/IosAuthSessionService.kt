package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.appinit

import cocoapods.FirebaseAuth.FIRAuth
import com.minhtu.firesocialmedia.data.remote.service.auth.appinit.AuthSessionService

class IosAuthSessionService : AuthSessionService {
    override suspend fun getCurrentUserUid(): String? {
        return FIRAuth.auth().currentUser()?.uid()
    }

    override suspend fun getCurrentUserEmail(): String? {
        return FIRAuth.auth().currentUser()?.email()
    }
}
