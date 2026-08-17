package com.minhtu.firesocialmedia.android.service.serviceimpl.auth.group

import com.google.firebase.auth.FirebaseAuth
import com.minhtu.firesocialmedia.data.remote.service.auth.group.AuthSessionService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidAuthSessionService : AuthSessionService {
    override suspend fun getCurrentUserUid(): String? = suspendCancellableCoroutine { continuation ->
        if (continuation.isActive) continuation.resume(FirebaseAuth.getInstance().uid)
    }

    override suspend fun getCurrentUserEmail(): String? = suspendCancellableCoroutine { continuation ->
        if (continuation.isActive) continuation.resume(FirebaseAuth.getInstance().currentUser?.email)
    }
}
