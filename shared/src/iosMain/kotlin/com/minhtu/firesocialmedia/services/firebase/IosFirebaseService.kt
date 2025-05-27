package com.minhtu.firesocialmedia.services.firebase

import com.minhtu.firesocialmedia.FirebaseService
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.signin.SignInState
import cocoapods.FirebaseDatabase.*
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toUserInstance

class IosFirebaseService : FirebaseService {
    override fun checkUserExists(email: String, callback: (SignInState) -> Unit) {
        val database = FIRDatabase.database()
        val ref = database.reference().child("users")

        ref.observeEventType(
            FIRDataEventType.FIRDataEventTypeValue,
            withBlock = { snapshot: FIRDataSnapshot? ->
                if (snapshot == null || !snapshot.exists()) {
                    callback(SignInState(true, Constants.ACCOUNT_NOT_EXISTED))
                    return@observeEventType
                }

                val children = snapshot.children

                var existed = false

                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val value = child.value as? Map<*,*> ?: continue
                    try {
                        val user = value.toUserInstance()
                        if (user.email == email) {
                            callback.invoke(SignInState(true, Constants.ACCOUNT_EXISTED))
                            existed = true
                            break
                        }
                    } catch (e: Exception) {
                        // If parsing fails, continue
                        continue
                    }
                }

                if (!existed) {
                    callback.invoke(SignInState(true, Constants.ACCOUNT_NOT_EXISTED))
                }
            },
            withCancelBlock = { error ->
                callback.invoke(SignInState(false, Constants.LOGIN_ERROR))
            }
        )
    }
}