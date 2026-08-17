package com.minhtu.firesocialmedia.platform

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.minhtu.firesocialmedia.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.data.remote.service.signinlauncher.SignInLauncher
import com.minhtu.firesocialmedia.di.PlatformContext

@Composable
actual fun setupSignInLauncher(
    context: Any?,
    signInViewModel: GoogleSignInHandler,
    platformContext: PlatformContext
) {
    val activity = when (context) {
        is Activity -> context
        is Context -> context
        else -> LocalContext.current
    }
    val signInGoogleResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            try {
                val task = Identity.getSignInClient(activity).getSignInCredentialFromIntent(result.data)
                signInViewModel.handleSignInResult(task)
            } catch (e: Exception) {
                logMessage("SignIn") { "Exception: ${e.message}" }
                signInViewModel.updateSignInStatus(false)
            }
        }
    )
    LaunchedEffect(Unit) {
        signInViewModel.setSignInLauncher(object : SignInLauncher {
            override fun launchGoogleSignIn() {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId("744458948813-qktjfopd2cr9b1a87pbr3981ujllb3mt.apps.googleusercontent.com")
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()
                val googleSignInClient = Identity.getSignInClient(activity)
                googleSignInClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                        signInGoogleResultLauncher.launch(intentSenderRequest)
                    } catch (e: android.content.IntentSender.SendIntentException) {
                        logMessage("OneTapSignIn") { "Error launching intent: ${e.localizedMessage}" }
                    }
                }.addOnFailureListener { exception ->
                    logMessage("OneTapSignIn") { "Sign-in failed: ${exception.localizedMessage}" }
                }
            }
        })
    }
}
