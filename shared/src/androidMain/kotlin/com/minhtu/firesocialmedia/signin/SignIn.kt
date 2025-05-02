package com.minhtu.firesocialmedia.signin

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.Identity
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel

class SignIn{
    companion object{
        @Composable
        fun SignInScreen(
            activity: Any,
            signInViewModel: SignInViewModel = viewModel(),
            loadingViewModel: LoadingViewModel = viewModel(),
            modifier: Modifier,
            onNavigateToSignUpScreen:() -> Unit,
            onNavigateToHomeScreen:()-> Unit,
            onNavigateToInformationScreen:() -> Unit,
            onNavigateToForgotPasswordScreen:() -> Unit) {
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val context = LocalContext.current
            val resultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {
                    result ->
                    try{
                        val task = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                        signInViewModel.handleSignInResult(task, activity)
                    } catch(e : Exception){
                        Log.e("SignIn", "Exception: ${e.message}")
                        signInViewModel.signInState.postValue(SignInState(false, Constants.LOGIN_ERROR))
                    }
                }
            )
            val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
            LaunchedEffect(lifecycleOwner.value) {
                //Check login information in storage
                signInViewModel.checkAccountInLocalStorage(context, loadingViewModel)
                signInViewModel.signInState.observe(lifecycleOwner.value){signInState ->
                    loadingViewModel.hideLoading()
                    if(signInState.signInStatus){
                        Toast.makeText(context,"Sign in successfully!!!", Toast.LENGTH_SHORT).show()
                        if(signInState.message == Constants.ACCOUNT_NOT_EXISTED) {
                            onNavigateToInformationScreen()
                        } else {
                            onNavigateToHomeScreen()
                        }
                    } else{
                        when(signInState.message){
                            Constants.DATA_EMPTY-> Toast.makeText(context,"Please fill all information!", Toast.LENGTH_SHORT).show()
                            Constants.LOGIN_ERROR -> Toast.makeText(context,"Error happened!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            //Back button
            QuitAlertDialog(context)

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                    //Title
                    Text(
                        text = "FireSocialMedia",
                        color = Color.Red,
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
                            .padding(20.dp)
                            .testTag(TestTag.TAG_USERNAME)
                            .semantics{
                                contentDescription = TestTag.TAG_USERNAME
                            },
                        shape = RoundedCornerShape(30.dp),
                        label = { Text(text = "Username")},
                        singleLine = true,
                        textStyle = TextStyle(Color.White)
                    )
                    //Password textfield
                    PasswordTextField(Constants.PASSWORD, signInViewModel, TestTag.TAG_PASSWORD)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Forgot password
                        Text(
                            text = "Forgot password?",
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .clickable {
                                    onNavigateToForgotPasswordScreen()
                                }
                                .testTag(TestTag.TAG_FORGOTPASSWORD)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        //Remember password
                        MyCheckbox(signInViewModel)

                    }
                    //Row contains buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //SignIn button
                        Button(onClick = {
                            loadingViewModel.showLoading()
                            signInViewModel.signIn(context)},
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_SIGNIN)) {
                            Text(text = "Sign In")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                        //SignUp button
                        Button(onClick = {onNavigateToSignUpScreen()},
                                modifier = Modifier.testTag(TestTag.TAG_BUTTON_SIGNUP)) {
                            Text(text = "Sign Up")
                        }
                    }
                    Text(
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        text = "Or register with",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //Google button
                        Button(onClick = {
                            signInViewModel.signInWithGoogle(context, resultLauncher)
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.95f),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .border(1.dp, Color.Black, RoundedCornerShape(30.dp))
                                .testTag(TestTag.TAG_BUTTON_SIGNINGOOGLE)
                        ) {
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
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        private fun QuitAlertDialog(context : Context) {
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

        @Composable
        fun PasswordTextField(label : String, signInViewModel: SignInViewModel, testTag: String) {
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
                    .testTag(testTag),
                shape = RoundedCornerShape(30.dp),
                label = { Text(text = label) },
                singleLine = true,
                textStyle = TextStyle(Color.White),
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

        @Composable
        fun MyCheckbox(signInViewModel: SignInViewModel
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag(TestTag.TAG_REMEMBERPASSWORD)
            ) {
                Checkbox(
                    checked = signInViewModel.rememberPassword,
                    onCheckedChange = { signInViewModel.updateRememberPassword(it)}
                )
                Text(
                    color = Color.White,
                    text = "Remember password",
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }

        fun getScreenName(): String{
            return "SignInScreen"
        }
    }
}