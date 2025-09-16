package com.minhtu.firesocialmedia.presentation.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.minhtu.firesocialmedia.platform.PasswordVisibilityIcon
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

class SignUp {
    companion object{
        @Composable
        fun SignUpScreen(
            signUpViewModel: SignUpViewModel,
            loadingViewModel: LoadingViewModel,
            modifier: Modifier,
            onNavigateToSignInScreen : () -> Unit,
            onNavigateToInformationScreen: ()-> Unit
        ){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            //Use launched effect to observe state one time although recomposition happened
            val signUpStatus = signUpViewModel.signUpStatus.collectAsState()
            LaunchedEffect(signUpStatus.value) {
                loadingViewModel.hideLoading()
                if (signUpStatus.value.signUpStatus) {
                    onNavigateToInformationScreen()
                } else {
                    when (signUpStatus.value.message) {
                        Constants.Companion.DATA_EMPTY -> showToast("Please fill all information!")
                        Constants.Companion.PASSWORD_MISMATCH -> showToast("Passwords are different!")
                        Constants.Companion.PASSWORD_SHORT -> showToast("Password is too short!")
                        Constants.Companion.SIGNUP_FAIL -> showToast("Sign up failed. Something went wrong!")
                    }
                }
                signUpViewModel.resetSignUpStatus()
            }
            Box(modifier = Modifier.Companion.fillMaxSize()) {
                Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                    //Title
                    Text(
                        text = "Sign Up",
                        color = Color.Companion.Red,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.Companion.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.Companion.padding(bottom = 30.dp))
                    //Username textfield
                    OutlinedTextField(
                        value = signUpViewModel.email,
                        textStyle = TextStyle(Color.Companion.White),
                        onValueChange = { email ->
                            signUpViewModel.updateEmail(email)
                        }, modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.Companion.TAG_USERNAME)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_USERNAME
                            },
                        label = { Text(text = "Username") },
                        shape = RoundedCornerShape(30.dp),
                        singleLine = true
                    )
                    //Password textfield
                    PasswordTextField(
                        Constants.Companion.PASSWORD,
                        signUpViewModel,
                        TestTag.Companion.TAG_PASSWORD
                    )
                    //Confirm password textfield
                    PasswordTextField(
                        Constants.Companion.CONFIRM_PASSWORD,
                        signUpViewModel,
                        TestTag.Companion.TAG_CONFIRMPASSWORD
                    )

                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //Back button
                        Button(
                            onClick = { onNavigateToSignInScreen() },
                            modifier = Modifier.Companion.testTag(TestTag.Companion.TAG_BUTTON_BACK)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_BACK
                                }) {
                            Text(text = "Back")
                        }
                        Spacer(modifier = Modifier.Companion.padding(horizontal = 20.dp))
                        //SignUp button
                        Button(
                            onClick = {
                                loadingViewModel.showLoading()
                                signUpViewModel.signUp()
                            },
                            modifier = Modifier.Companion
                                .testTag(TestTag.Companion.TAG_BUTTON_SIGNUP)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_SIGNUP
                                }) {
                            Text(text = "Sign Up")
                        }
                    }
                }
                if (isLoading) {
                    Loading.Companion.LoadingScreen()
                }
            }
        }

        @Composable
        fun PasswordTextField(label : String, signUpViewModel: SignUpViewModel, testTag: String) {
            var passwordVisibility by rememberSaveable {
                mutableStateOf(false)
            }
            OutlinedTextField(
                value = if (label == Constants.Companion.PASSWORD) signUpViewModel.password else signUpViewModel.confirmPassword,
                onValueChange = { password ->
                    if (label == Constants.Companion.PASSWORD) signUpViewModel.updatePassword(
                        password
                    )
                    else signUpViewModel.updateConfirmPassword(password)
                },
                textStyle = TextStyle(Color.Companion.White),
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics {
                        contentDescription = testTag
                    },
                label = { Text(text = label) },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
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
            return "SignUpScreen"
        }
    }
}