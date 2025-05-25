package com.minhtu.firesocialmedia.forgotpassword

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
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.showToast

class ForgotPassword{
    companion object{
        @Composable
        fun ForgotPasswordScreen(
            platform: PlatformContext,
            forgotPasswordViewModel: ForgotPasswordViewModel,
            loadingViewModel: LoadingViewModel,
            modifier: Modifier,
            onNavigateToSignInScreen:() -> Unit) {
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val emailExisted = forgotPasswordViewModel.emailExisted.collectAsState()
            val emailSent = forgotPasswordViewModel.emailSent.collectAsState()
            LaunchedEffect(emailExisted.value) {
                if (emailExisted.value != null) {
                    if(emailExisted.value!!.first) {
                        forgotPasswordViewModel.sendEmailResetPassword(platform)
                    } else {
                        when(emailExisted.value!!.second) {
                            Constants.EMAIL_EMPTY -> {
                                showToast("Please input your email!")
                            }
                            Constants.EMAIL_SERVER_ERROR -> {
                                showToast("Server error happened! Please try again.")
                            }
                            Constants.EMAIL_NOT_EXISTED -> {
                                showToast("This email doesn't exist!")
                            }
                        }
                    }
                }
            }
            LaunchedEffect(emailSent.value) {
                if(emailSent.value !=  null) {
                    if(emailSent.value!!) {
                        showToast("Please check your email to reset password!")
                        onNavigateToSignInScreen()
                    } else {
                        showToast("Server error happened! Please try again.")
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                    //Title
                    Text(
                        text = "Forgot Password",
                        color = Color.Blue,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(bottom = 50.dp))
                    //Username textfield
                    OutlinedTextField(
                        value = forgotPasswordViewModel.email, onValueChange = {
                            forgotPasswordViewModel.updateEmail(it)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.TAG_USERNAME)
                            .semantics{
                                contentDescription = TestTag.TAG_USERNAME
                            },
                        label = { Text(text = "Username")},
                        singleLine = true
                    )

                    //Row contains buttons
                    Row(
                        modifier = Modifier
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
                        Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                        //Reset button
                        Button(onClick = {forgotPasswordViewModel.checkIfEmailExists(platform)}) {
                            Text(text = "Reset Password")
                        }
                    }
                }
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        fun getScreenName(): String{
            return "ForgotPasswordScreen"
        }
    }
}