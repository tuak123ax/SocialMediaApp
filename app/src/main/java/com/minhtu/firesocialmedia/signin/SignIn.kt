package com.minhtu.firesocialmedia.signin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.Constants

class SignIn {
    companion object{
        @Composable
        fun SignInScreen(
            activity: Activity,
            signInViewModel: SignInViewModel = viewModel(),
            modifier: Modifier,
            onNavigateToSignUpScreen:() -> Unit,
            onNavigateToHomeScreen:()-> Unit) {
            val context = LocalContext.current
            val resultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {
                    result ->
                    try{
                        val task = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                        handleSignInResult(task, activity, onNavigateToHomeScreen)
                    } catch(e : Exception){
                        Log.e("SignIn", "Exception: ${e.message}")
                    }
                }
            )
            val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
            LaunchedEffect(lifecycleOwner.value) {
                signInViewModel.signInState.observe(lifecycleOwner.value){signInState ->
                    if(signInState.signInStatus){
                        Toast.makeText(context,"Sign in successfully!!!", Toast.LENGTH_SHORT).show()
                        onNavigateToHomeScreen()
                    } else{
                        when(signInState.message){
                            Constants.DATA_EMPTY-> Toast.makeText(context,"Please fill all information!", Toast.LENGTH_SHORT).show()
                            Constants.LOGIN_ERROR -> Toast.makeText(context,"Error happened!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            //Back button
            showAlertDialog(context)

            Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                //Title
                Text(
                    text = "FireMedia",
                    color = Color.Blue,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(bottom = 50.dp))
                //Username textfield
                OutlinedTextField(
                    value = signInViewModel.email, onValueChange = {
                        signInViewModel.updateEmail(it)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    label = { Text(text = "Username")},
                    singleLine = true
                )
                //Password textfield
                PasswordTextField(Constants.PASSWORD, signInViewModel)
                //Row contains buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), horizontalArrangement = Arrangement.Center
                ) {
                    //SignIn button
                    Button(onClick = { signInViewModel.signIn(context) }) {
                        Text(text = "Sign In")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                    //SignUp button
                    Button(onClick = {onNavigateToSignUpScreen()}) {
                        Text(text = "Sign Up")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), horizontalArrangement = Arrangement.Center
                ) {
                    //Google button
                    Button(onClick = {
                        setupGoogleSignIn(context, resultLauncher)
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

        private fun setupGoogleSignIn(context: Context, resultLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>){
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId("744458948813-qktjfopd2cr9b1a87pbr3981ujllb3mt.apps.googleusercontent.com")
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build()
            val googleSignInClient = Identity.getSignInClient(context)
            googleSignInClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
                try {
                    // Launch the One Tap UI
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                    resultLauncher.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("OneTapSignIn", "Error launching intent: ${e.localizedMessage}")
                }
            }
                .addOnFailureListener { exception ->
                    Log.e("OneTapSignIn", "Sign-in failed: ${exception.localizedMessage}")
                }
        }

        @Composable
        private fun showAlertDialog(context : Context) {
            var showDialog by remember { mutableStateOf(false) }
            BackHandler {
                showDialog = true
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Exit App") },
                    text = { Text("Are you sure you want to exit?") },
                    confirmButton = {
                        Button(onClick = {(context as Activity).finish()}) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }

        private fun handleSignInResult(credential : SignInCredential, activity: Activity, onNavigateToHomeScreen: () -> Unit) {
            val idToken = credential.googleIdToken
            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate
                    // with Firebase.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    Firebase.auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Signin", "signInWithCredential:success")
                                val user = Firebase.auth.currentUser
                                onNavigateToHomeScreen()
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Signin", "signInWithCredential:failure", task.exception)
                            }
                        }
                }
                else -> {
                    // Shouldn't happen.
                    Log.d("Signin", "No ID token!")
                }
            }
        }

        @Composable
        fun PasswordTextField(label : String, signInViewModel: SignInViewModel) {
            var passwordVisibility by rememberSaveable {
                mutableStateOf(false)
            }
            OutlinedTextField(
                value = signInViewModel.password,
                onValueChange = {
                        password -> signInViewModel.updatePassword(password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clearAndSetSemantics { },
                label = { Text(text = label) },
                singleLine = true,
                visualTransformation = if(passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if(passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
                    IconButton(onClick = {passwordVisibility = !passwordVisibility}) {
                        Icon(imageVector = icon, descriptionOfIcon)
                    }
                }
            )
        }

        fun getScreenName(): String{
            return "SignInScreen"
        }
    }
}