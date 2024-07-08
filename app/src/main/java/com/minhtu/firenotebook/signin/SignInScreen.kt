package com.minhtu.firenotebook.signin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.minhtu.firenotebook.R

class SignInScreen {
    companion object{
        @Composable
        fun LogInScreen(
            signInViewModel: SignInViewModel = viewModel(),
            modifier: Modifier) {
            val context = LocalContext.current
            val resultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = {
                    result ->
                    if(result.resultCode == Activity.RESULT_OK){
                        val data = result.data
                        handleData(data, context, signInViewModel)
                    }

                }
            )
            val signInStatus by signInViewModel.signInStatus.collectAsState()
            var email by remember {
                mutableStateOf("")
            }
            var password by remember {
                mutableStateOf("")
            }
            Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                Text(
                    text = "FireNotebook",
                    color = Color.Blue,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(bottom = 50.dp))
                OutlinedTextField(
                    value = email, onValueChange = {
                        email = it
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
                OutlinedTextField(
                    value = password, onValueChange = {
                        password = it
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {}) {
                        Text(text = "Sign In")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                    Button(onClick = {}) {
                        Text(text = "Sign Up")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {

                    }, colors = ButtonDefaults.buttonColors(Color.White)) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google",
                            modifier = Modifier
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = "Sign In With Google", color = Color.Black)
                    }
                }
            }
        }

        private fun handleData(data: Intent?, context : Context, signInViewModel: SignInViewModel) {
            try {
                val credential = SignInActivity.oneTapClient!!.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val auth = Firebase.auth
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener{
                                    task ->
                                if(task.isSuccessful){
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("SignIn", "signInWithCredential:success")
                                    val user = auth.currentUser
                                    signInViewModel.updateUI(user, context = context)
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("SignIn", "signInWithCredential:failure", task.exception)
                                    signInViewModel.updateUI(null, context)
                                }
                            }
                        Log.e("Token", "Got ID token.")
                    }

                    else -> {
                        // Shouldn't happen.
                        Log.e("Token", "No ID token!")
                    }
                }
            } catch (e: ApiException) {
                Log.e("Token", "Api Exception!")
            }
        }
    }
}