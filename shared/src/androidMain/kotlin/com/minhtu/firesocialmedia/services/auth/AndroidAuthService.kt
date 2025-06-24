package com.minhtu.firesocialmedia.services.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.di.AuthService
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.presentation.signin.SignInError
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class AndroidAuthService(var context: Context) : AuthService{
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await() // suspend until complete
            Result.success(Unit)
        } catch (ex: Exception) {
            // You can parse exception here to map to your custom error
            Result.failure(SignInError.Unknown(ex.message ?: "Unknown error"))
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .await() // suspend until complete
            Result.success(Unit)
        } catch (ex: Exception) {
            // You can parse exception here to map to your custom error
            Result.failure(SignInError.Unknown(ex.message ?: "Unknown error"))
        }
    }

    override fun getCurrentUserUid(): String? {
        return FirebaseAuth.getInstance().uid
    }

    override fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email.toString()
    }

    override fun fetchSignInMethodsForEmail(
        email: String,
        stateFlow: MutableStateFlow<Pair<Boolean, String>?>
    ) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        stateFlow.value = Pair(false, Constants.EMAIL_NOT_EXISTED)
                    } else {
                        stateFlow.value = Pair(true, Constants.EMAIL_EXISTED)
                    }
                } else {
                    stateFlow.value = Pair(false, Constants.EMAIL_SERVER_ERROR)
                }
            }
    }

    override fun sendPasswordResetEmail(
        email: String,
        stateFlow: MutableStateFlow<Boolean?>
    ) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    stateFlow.value = true
                } else {
                    stateFlow.value = false
                }
            }
    }

    override fun handleSignInGoogleResult(
        credential: Any,
        callback: Utils.Companion.SignInGoogleCallback
    ) {
        val idToken = (credential as SignInCredential).googleIdToken
        when {
            idToken != null -> {
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                Firebase.auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(context as Activity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            logMessage("Signin", "signInWithCredential:success")
                            val user = Firebase.auth.currentUser
                            callback.onSuccess(user!!.email.toString())
                        } else {
                            // If sign in fails, display a message to the user.
                            logMessage("SignInViewModel","signIn: LOGIN_ERROR")
                            callback.onFailure()
                        }
                    }
            }
            else -> {
                // Shouldn't happen.
                logMessage("Signin", "No ID token!")
                callback.onFailure()
            }
        }
    }
}