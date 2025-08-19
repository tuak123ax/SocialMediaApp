package com.minhtu.firesocialmedia.presentation.signin

import androidx.compose.foundation.border
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
import com.minhtu.firesocialmedia.di.PlatformContext
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
            platform : PlatformContext,
            signInViewModel: SignInViewModel,
            loadingViewModel: LoadingViewModel,
            modifier: Modifier,
            onNavigateToSignUpScreen:() -> Unit,
            onNavigateToHomeScreen:()-> Unit,
            onNavigateToInformationScreen:() -> Unit,
            onNavigateToForgotPasswordScreen:() -> Unit) {
            val isLoading = loadingViewModel.isLoading.collectAsState()

            LaunchedEffect(Unit) {
                //Check login information in storage
                signInViewModel.checkLocalAccount(platform) { loadingViewModel.showLoading() }
            }
            val signInStatus = signInViewModel.signInState.collectAsState()
            LaunchedEffect(signInStatus.value) {
                loadingViewModel.hideLoading()
                if (signInStatus.value.signInStatus) {
                    if (signInStatus.value.message == Constants.Companion.ACCOUNT_NOT_EXISTED) {
                        onNavigateToInformationScreen()
                    } else {
                        onNavigateToHomeScreen()
                    }
                } else {
                    when (signInStatus.value.message) {
                        Constants.Companion.DATA_EMPTY -> showToast("Please fill all information!")
                        Constants.Companion.LOGIN_ERROR -> showToast("Error happened!")
                    }
                }
                signInViewModel.resetSignInStatus()
            }

            //Back button
            QuitAlertDialog()

            Box(modifier = Modifier.Companion.fillMaxSize()) {
                Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                    //Title
                    Text(
                        text = "FireSocialMedia",
                        color = Color.Companion.Red,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.Companion.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.Companion.padding(bottom = 50.dp))
                    //Username textfield
                    OutlinedTextField(
                        value = signInViewModel.email.collectAsState().value,
                        onValueChange = { text ->
                            signInViewModel.updateEmail(text)
                        }, modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp)
                            .focusable(true)
                            .testTag(TestTag.Companion.TAG_USERNAME)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_USERNAME
                            },
                        shape = RoundedCornerShape(30.dp),
                        label = { Text(text = "Username") },
                        singleLine = true,
                        textStyle = TextStyle(Color.Companion.White)
                    )
                    //Password textfield
                    PasswordTextField(
                        Constants.Companion.PASSWORD,
                        signInViewModel,
                        TestTag.Companion.TAG_PASSWORD
                    )

                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Companion.CenterVertically
                    ) {
                        //Forgot password
                        Text(
                            text = "Forgot password?",
                            color = Color.Companion.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Companion.Start,
                            modifier = Modifier.Companion
                                .clickable {
                                    onNavigateToForgotPasswordScreen()
                                }
                                .testTag(TestTag.Companion.TAG_FORGOTPASSWORD)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_FORGOTPASSWORD
                                }
                        )
                        Spacer(modifier = Modifier.Companion.weight(1f))
                        //Remember password
                        MyCheckbox(signInViewModel)

                    }
                    //Row contains buttons
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //SignIn button
                        Button(
                            onClick = {
                                signInViewModel.signIn(
                                    showLoading = { loadingViewModel.showLoading() },
                                    platform
                                )
                            },
                            modifier = Modifier.Companion.testTag(TestTag.Companion.TAG_BUTTON_SIGNIN)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_SIGNIN
                                }) {
                            Text(text = "Sign In")
                        }
                        Spacer(modifier = Modifier.Companion.padding(horizontal = 20.dp))
                        //SignUp button
                        Button(
                            onClick = { onNavigateToSignUpScreen() },
                            modifier = Modifier.Companion
                                .testTag(TestTag.Companion.TAG_BUTTON_SIGNUP)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_SIGNUP
                                }) {
                            Text(text = "Sign Up")
                        }
                    }
                    Text(
                        textAlign = TextAlign.Companion.Center,
                        color = Color.Companion.White,
                        text = "Or register with",
                        modifier = Modifier.Companion.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //Google button
                        OutlinedButton(
                            onClick = {
                                signInViewModel.signInWithGoogle()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Companion.White,
                                contentColor = Color.Companion.Black
                            ),
                            modifier = Modifier.Companion
                                .border(
                                    1.dp,
                                    Color.Companion.Black,
                                    androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
                                )
                                .testTag(TestTag.Companion.TAG_BUTTON_SIGNINGOOGLE)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_SIGNINGOOGLE
                                }
                        ) {
                            CrossPlatformIcon(
                                "google",
                                backgroundColor = "#FFFFFFFF",
                                "Google",
                                Modifier.Companion
                                    .size(25.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = "Sign In With Google", color = Color.Companion.Black)
                        }
                    }
                }
                if (isLoading.value) {
                    Loading.Companion.LoadingScreen()
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
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Checkbox(
                    checked = signInViewModel.rememberPassword.collectAsState().value,
                    onCheckedChange = { signInViewModel.updateRememberPassword(it) },
                    modifier = Modifier.Companion
                        .testTag(TestTag.Companion.TAG_REMEMBERPASSWORD)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_REMEMBERPASSWORD
                        }
                )
                Text(
                    color = Color.Companion.White,
                    text = "Remember password",
                    fontSize = 10.sp,
                    modifier = Modifier.Companion.padding(start = 5.dp)
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
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics {
                        contentDescription = testTag
                    },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                label = { Text(text = label) },
                singleLine = true,
                textStyle = TextStyle(Color.Companion.White),
                visualTransformation = if (passwordVisibility) VisualTransformation.Companion.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
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