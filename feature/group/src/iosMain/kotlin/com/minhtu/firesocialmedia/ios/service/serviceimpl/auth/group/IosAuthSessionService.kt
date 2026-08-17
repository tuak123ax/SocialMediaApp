package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.group

import cocoapods.FirebaseAuth.FIRAuth
import com.minhtu.firesocialmedia.data.remote.service.auth.group.AuthSessionService

class IosAuthSessionService : AuthSessionService {
    override suspend fun getCurrentUserUid(): String? {
        return FIRAuth.auth().currentUser()?.uid()
    }

    override suspend fun getCurrentUserEmail(): String? {
        return FIRAuth.auth().currentUser()?.email()
    }
}
