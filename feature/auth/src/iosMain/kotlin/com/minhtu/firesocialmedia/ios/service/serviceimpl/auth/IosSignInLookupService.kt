package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.SignInLookupService
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

private fun <T> CancellableContinuation<T>.resume(value: T) {
    this.resume(value, onCancellation = null)
}

class IosSignInLookupService : SignInLookupService {
    override suspend fun checkUserExists(email: String): SignInDTO = suspendCancellableCoroutine { continuation ->
        val database = FIRDatabase.database()
        val ref = database.reference().child("users")

        ref.observeEventType(
            FIRDataEventType.FIRDataEventTypeValue,
            withBlock = { snapshot: FIRDataSnapshot? ->
                if (snapshot == null || !snapshot.exists()) {
                    if (continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_NOT_EXISTED))
                    return@observeEventType
                }

                val children = snapshot.children

                var existed = false

                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val value = child.value as? Map<*, *> ?: continue
                    val userEmail = value["email"] as? String
                    if (userEmail == email) {
                        if (continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_EXISTED))
                        existed = true
                        break
                    }
                }

                if (!existed) {
                    if (continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_NOT_EXISTED))
                }
            },
            withCancelBlock = { error ->
                if (continuation.isActive) continuation.resume(SignInDTO(false, Constants.LOGIN_ERROR))
            }
        )
    }
}
