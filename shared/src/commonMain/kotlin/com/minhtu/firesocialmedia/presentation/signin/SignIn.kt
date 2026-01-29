package com.minhtu.firesocialmedia.presentation.signin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.PasswordVisibilityIcon
import com.minhtu.firesocialmedia.platform.exitApp
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

class SignIn{
    companion object{
        @Composable
        fun SignInScreen(
            signInViewModel: SignInViewModel,
            loadingViewModel: LoadingViewModel,
            modifier: Modifier,
            onNavigateToSignUpScreen:() -> Unit,
            onNavigateToHomeScreen:()-> Unit,
            onNavigateToInformationScreen:() -> Unit,
            onNavigateToForgotPasswordScreen:() -> Unit) {
            val isLoading = loadingViewModel.isLoading.collectAsState()

            val localCredentials = signInViewModel.localCredentials
            LaunchedEffect(Unit) {
                //Check login information in storage
                signInViewModel.checkLocalAccount()
            }
            LaunchedEffect(localCredentials.value) {
                if(localCredentials.value != null) {
                    signInViewModel.signIn(
                        showLoading = {
                            loadingViewModel.showLoading()
                        }
                    )
                }
            }
            val signInStatus = signInViewModel.signInState.collectAsState()
            LaunchedEffect(signInStatus.value) {
                loadingViewModel.hideLoading()
                if (signInStatus.value.signInStatus) {
                    if (signInStatus.value.error == SignInError.AccountNotExist) {
                        onNavigateToInformationScreen()
                    } else {
                        onNavigateToHomeScreen()
                    }
                } else {
                    when (signInStatus.value.error) {
                        SignInError.DataEmpty -> showToast("Please fill all information!")
                        SignInError.InvalidCredentials -> showToast("Your email or password is invalid!")
                        SignInError.InvalidEmail -> showToast("Your email is invalid!")
                        SignInError.InvalidUser -> showToast("Your user is invalid!")
                        SignInError.MultiFactor -> showToast("Multi factor error happened!")
                        SignInError.NetworkError -> showToast("Please recheck your network!")
                        SignInError.TooManyRequests -> showToast("Too many requests! Slow down please!")
                        SignInError.UserDisabled -> showToast("User is disable!")
                        SignInError.UserNotFound -> showToast("User is not found!")
                        SignInError.WrongPassword -> showToast("Your password is wrong!")
                        is SignInError.Unknown -> showToast("Error happened!")
                        else -> {
                            //Do nothing
                        }
                    }
                }
                signInViewModel.resetSignInStatus()
            }

            //Back button
            QuitAlertDialog()

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
                        value = signInViewModel.email.collectAsState().value,
                        onValueChange = { text ->
                            signInViewModel.updateEmail(text)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .focusable(true)
                            .testTag(TestTag.TAG_USERNAME)
                            .semantics {
                                contentDescription = TestTag.TAG_USERNAME
                            },
                        shape = RoundedCornerShape(30.dp),
                        label = { Text(text = "Username") },
                        singleLine = true,
                        textStyle = TextStyle(Color.White)
                    )
                    //Password textfield
                    PasswordTextField(
                        Constants.PASSWORD,
                        signInViewModel,
                        TestTag.TAG_PASSWORD
                    )

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
                                .semantics {
                                    contentDescription = TestTag.TAG_FORGOTPASSWORD
                                }
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
                        Button(
                            onClick = {
                                signInViewModel.signIn(
                                    showLoading = { loadingViewModel.showLoading() }
                                )
                            },
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_SIGNIN)
                                .semantics {
                                    contentDescription = TestTag.TAG_BUTTON_SIGNIN
                                }) {
                            Text(text = "Sign In")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                        //SignUp button
                        Button(
                            onClick = { onNavigateToSignUpScreen() },
                            modifier = Modifier
                                .testTag(TestTag.TAG_BUTTON_SIGNUP)
                                .semantics {
                                    contentDescription = TestTag.TAG_BUTTON_SIGNUP
                                }) {
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
                        OutlinedButton(
                            onClick = {
                                signInViewModel.signInWithGoogle()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .testTag(TestTag.TAG_BUTTON_SIGNINGOOGLE)
                                .semantics {
                                    contentDescription = TestTag.TAG_BUTTON_SIGNINGOOGLE
                                }
                        ) {
                            CrossPlatformIcon(
                                "google",
                                backgroundColor = "#FFFFFFFF",
                                "Google",
                                Modifier
                                    .size(25.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = "Sign In With Google", color = Color.Black)
                        }
                    }
                }
                if (isLoading.value) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun QuitAlertDialog() {
            var showDialog by remember { mutableStateOf(false) }
            CommonBackHandler {
                showDialog = true
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Exit App") },
                    text = { Text("Are you sure you want to exit?") },
                    confirmButton = {
                        Button(onClick = { exitApp() }) {
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
        fun MyCheckbox(signInViewModel: SignInViewModel) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = signInViewModel.rememberPassword.collectAsState().value,
                    onCheckedChange = { signInViewModel.updateRememberPassword(it) },
                    modifier = Modifier
                        .testTag(TestTag.TAG_REMEMBERPASSWORD)
                        .semantics {
                            contentDescription = TestTag.TAG_REMEMBERPASSWORD
                        }
                )
                Text(
                    color = Color.White,
                    text = "Remember password",
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }

        @Composable
        fun PasswordTextField(label : String, signInViewModel: SignInViewModel, testTag: String) {
            var passwordVisibility by rememberSaveable {
                mutableStateOf(false)
            }
            OutlinedTextField(
                value = signInViewModel.password.collectAsState().value,
                onValueChange = { password ->
                    signInViewModel.updatePassword(password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics {
                        contentDescription = testTag
                    },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                label = { Text(text = label) },
                singleLine = true,
                textStyle = TextStyle(Color.White),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisibility = !passwordVisibility },
                        modifier = Modifier
                            .testTag(TestTag.TAG_SHOW_PASSWORD)
                            .semantics{
                                contentDescription = TestTag.TAG_SHOW_PASSWORD
                            }
                    ) {
                        PasswordVisibilityIcon(passwordVisibility)
                    }
                }
            )
        }

        fun getScreenName(): String{
            return "SignInScreen"
        }
    }
}