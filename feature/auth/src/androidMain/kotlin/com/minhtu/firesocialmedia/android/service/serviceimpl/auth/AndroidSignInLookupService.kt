package com.minhtu.firesocialmedia.android.service.serviceimpl.auth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.SignInLookupService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidSignInLookupService : SignInLookupService {
    override suspend fun checkUserExists(email: String): SignInDTO =
        suspendCancellableCoroutine { continuation ->
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child("users")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!continuation.isActive) return
                    val exists = snapshot.children.any {
                        it.getValue(UserDTO::class.java)?.email == email
                    }
                    val result = if (exists) {
                        SignInDTO(true, Constants.ACCOUNT_EXISTED)
                    } else {
                        SignInDTO(true, Constants.ACCOUNT_NOT_EXISTED)
                    }
                    continuation.resume(result)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!continuation.isActive) return
                    continuation.resume(SignInDTO(false, Constants.LOGIN_ERROR))
                }
            }
            databaseReference.addValueEventListener(listener)
            continuation.invokeOnCancellation { databaseReference.removeEventListener(listener) }
        }
}
