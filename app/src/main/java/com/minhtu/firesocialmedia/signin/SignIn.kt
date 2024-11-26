package com.minhtu.firesocialmedia.signin

import android.widget.Toast
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.Constants

class SignIn {
    companion object{
        @Composable
        fun SignInScreen(
            signInViewModel: SignInViewModel = viewModel(),
            modifier: Modifier,
            onNavigateToSignUpScreen:() -> Unit,
            onNavigateToHomeScreen:()-> Unit) {
//            val context = LocalContext.current
//            val resultLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.StartActivityForResult(),
//                onResult = {
//                    result ->
//                    if(result.resultCode == Activity.RESULT_OK){
//                        val data = result.data
//                        handleData(data, context, signInViewModel)
//                    }
//
//                }
//            )
            val context = LocalContext.current
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
                    //Fix crash: java.lang.IllegalStateException: Already in the pool! when using visualTransformation
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

//        private fun handleData(data: Intent?, context : Context, signInViewModel: SignInViewModel) {
//            try {
//                val credential = SignInActivity.oneTapClient!!.getSignInCredentialFromIntent(data)
//                val idToken = credential.googleIdToken
//                when {
//                    idToken != null -> {
//                        // Got an ID token from Google. Use it to authenticate
//                        // with Firebase.
//                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
//                        val auth = Firebase.auth
//                        auth.signInWithCredential(firebaseCredential)
//                            .addOnCompleteListener{
//                                    task ->
//                                if(task.isSuccessful){
//                                    // Sign in success, update UI with the signed-in user's information
//                                    Log.d("SignIn", "signInWithCredential:success")
//                                    val user = auth.currentUser
//                                    signInViewModel.updateUI(user, context = context)
//                                } else {
//                                    // If sign in fails, display a message to the user.
//                                    Log.w("SignIn", "signInWithCredential:failure", task.exception)
//                                    signInViewModel.updateUI(null, context)
//                                }
//                            }
//                        Log.e("Token", "Got ID token.")
//                    }
//
//                    else -> {
//                        // Shouldn't happen.
//                        Log.e("Token", "No ID token!")
//                    }
//                }
//            } catch (e: ApiException) {
//                Log.e("Token", "Api Exception!")
//            }
//        }
    }
}