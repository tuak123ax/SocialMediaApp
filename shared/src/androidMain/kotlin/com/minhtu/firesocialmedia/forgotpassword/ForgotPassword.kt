package com.minhtu.firesocialmedia.forgotpassword

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel

class ForgotPassword{
    companion object{
        @Composable
        fun ForgotPasswordScreen(
            forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(),
            loadingViewModel: LoadingViewModel = viewModel(),
            modifier: Modifier,
            onNavigateToSignInScreen:() -> Unit) {
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                forgotPasswordViewModel.emailExisted.observe(lifecycleOwner){emailExisted ->
                    Log.e("ForgotPasswordScreen", "receive emailExisted")
                    if(emailExisted.first) {
                        forgotPasswordViewModel.sendEmailResetPassword()
                    } else {
                        when(emailExisted.second) {
                            Constants.EMAIL_EMPTY -> {
                                Toast.makeText(context, "Please input your email!", Toast.LENGTH_SHORT).show()
                            }
                            Constants.EMAIL_SERVER_ERROR -> {
                                Toast.makeText(context, "Server error happened! Please try again.", Toast.LENGTH_SHORT).show()
                            }
                            Constants.EMAIL_NOT_EXISTED -> {
                            Toast.makeText(context, "This email doesn't exist!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                forgotPasswordViewModel.emailSent.observe(lifecycleOwner) { emailSent ->
                    Log.e("ForgotPasswordScreen", "receive emailSent")
                    if(emailSent) {
                        Toast.makeText(context,"Please check your email to reset password!", Toast.LENGTH_SHORT).show()
                        onNavigateToSignInScreen()
                    } else {
                        Toast.makeText(context,"Server error happened! Please try again.", Toast.LENGTH_SHORT).show()
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
                            .testTag(TestTag.TAG_USERNAME),
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
                            forgotPasswordViewModel.emailExisted.removeObservers(lifecycleOwner)
                            onNavigateToSignInScreen()
                        }) {
                            Text(text = "Back")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                        //Reset button
                        Button(onClick = {forgotPasswordViewModel.checkIfEmailExists()}) {
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