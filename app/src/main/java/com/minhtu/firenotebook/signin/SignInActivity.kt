package com.minhtu.firenotebook.signin

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.minhtu.firenotebook.R
import com.minhtu.firenotebook.ui.theme.FireNotebookTheme

class SignInActivity : ComponentActivity() {
    companion object{
        var signInRequest: BeginSignInRequest? = null
        var oneTapClient: SignInClient? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpGoogleSignIn(applicationContext)
        setContent {
            FireNotebookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignInScreen.LogInScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .paint(
                                painter = painterResource(id = R.drawable.background),
                                contentScale = ContentScale.FillBounds
                            )
                    )
                }
            }
        }
    }

    fun setUpGoogleSignIn(context : Context){
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(ContextCompat.getString(context, R.string.server_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
        oneTapClient = Identity.getSignInClient(context)
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun GreetingPreview() {
        FireNotebookTheme {
            SignInScreen.LogInScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painter = painterResource(id = R.drawable.background),
                        contentScale = ContentScale.FillBounds
                    )
            )
        }
    }
}