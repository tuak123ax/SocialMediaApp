package com.minhtu.firesocialmedia.presentation.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

class ForgotPassword{
    companion object{
        @Composable
        fun ForgotPasswordScreen(
            forgotPasswordViewModel: ForgotPasswordViewModel,
            loadingViewModel: LoadingViewModel,
            modifier: Modifier,
            onNavigateToSignInScreen:() -> Unit) {
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val emailExisted = forgotPasswordViewModel.emailExisted.collectAsState()
            val emailSent = forgotPasswordViewModel.emailSent.collectAsState()
            LaunchedEffect(emailExisted.value) {
                if (emailExisted.value != null) {
                    if (emailExisted.value!!.exist) {
                        forgotPasswordViewModel.sendEmailResetPassword()
                    } else {
                        when (emailExisted.value!!.message) {
                            Constants.Companion.EMAIL_EMPTY -> {
                                showToast("Please input your email!")
                            }

                            Constants.Companion.EMAIL_SERVER_ERROR -> {
                                showToast("Server error happened! Please try again.")
                            }

                            Constants.Companion.EMAIL_NOT_EXISTED -> {
                                showToast("This email doesn't exist!")
                            }
                        }
                    }
                }
            }
            LaunchedEffect(emailSent.value) {
                if (emailSent.value != null) {
                    if (emailSent.value!!) {
                        showToast("Please check your email to reset password!")
                        onNavigateToSignInScreen()
                    } else {
                        showToast("Server error happened! Please try again.")
                    }
                }
            }

            Box(modifier = Modifier.Companion.fillMaxSize()) {
                Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                    //Title
                    Text(
                        text = "Forgot Password",
                        color = Color.Companion.Blue,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.Companion.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.Companion.padding(bottom = 50.dp))
                    //Username textfield
                    OutlinedTextField(
                        value = forgotPasswordViewModel.email, onValueChange = {
                            forgotPasswordViewModel.updateEmail(it)
                        }, modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.Companion.TAG_USERNAME)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_USERNAME
                            },
                        label = { Text(text = "Username") },
                        singleLine = true
                    )

                    //Row contains buttons
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //Back button
                        Button(onClick = {
                            forgotPasswordViewModel.resetEmailResetPassword()
                            onNavigateToSignInScreen()
                        }) {
                            Text(text = "Back")
                        }
                        Spacer(modifier = Modifier.Companion.padding(horizontal = 20.dp))
                        //Reset button
                        Button(onClick = { forgotPasswordViewModel.checkIfEmailExists() }) {
                            Text(text = "Reset Password")
                        }
                    }
                }
                if (isLoading) {
                    Loading.Companion.LoadingScreen()
                }
            }
        }

        fun getScreenName(): String{
            return "ForgotPasswordScreen"
        }
    }
}