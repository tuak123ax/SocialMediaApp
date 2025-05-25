package com.minhtu.firesocialmedia.signup

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
import com.minhtu.firesocialmedia.PasswordVisibilityIcon
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.showToast

class SignUp {
    companion object{
        @Composable
        fun SignUpScreen(
            platform : PlatformContext,
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
                    when(signUpStatus.value.message){
                        Constants.DATA_EMPTY ->  showToast("Please fill all information!")
                        Constants.PASSWORD_MISMATCH -> showToast("Passwords are different!")
                        Constants.PASSWORD_SHORT -> showToast("Password is too short!")
                        Constants.SIGNUP_FAIL -> showToast("Sign up failed. Something went wrong!")
                    }
                }
                signUpViewModel.resetSignUpStatus()
            }
            Box(modifier = Modifier.fillMaxSize()){
                Column(modifier = modifier, verticalArrangement = Arrangement.Center){
                    //Title
                    Text(
                        text = "Sign Up",
                        color = Color.White,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(bottom = 30.dp))
                    //Username textfield
                    OutlinedTextField(
                        value = signUpViewModel.email,
                        textStyle = TextStyle(Color.White),
                        onValueChange = {
                                email -> signUpViewModel.updateEmail(email)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.TAG_USERNAME)
                            .semantics{
                                contentDescription = TestTag.TAG_USERNAME
                            },
                        label = { Text(text = "Username")},
                        shape = RoundedCornerShape(30.dp),
                        singleLine = true
                    )
                    //Password textfield
                    PasswordTextField(Constants.PASSWORD, signUpViewModel, TestTag.TAG_PASSWORD)
                    //Confirm password textfield
                    PasswordTextField(Constants.CONFIRM_PASSWORD, signUpViewModel, TestTag.TAG_CONFIRMPASSWORD)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //Back button
                        Button(onClick = {onNavigateToSignInScreen()},
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_BACK)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_BACK
                                }) {
                            Text(text = "Back")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                        //SignUp button
                        Button(onClick = {
                            loadingViewModel.showLoading()
                            signUpViewModel.signUp(platform) },
                            modifier = Modifier
                                .testTag(TestTag.TAG_BUTTON_SIGNUP)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_SIGNUP
                                }) {
                            Text(text = "Sign Up")
                        }
                    }
                }
                if(isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun PasswordTextField(label : String, signUpViewModel: SignUpViewModel, testTag: String) {
            var passwordVisibility by rememberSaveable {
                mutableStateOf(false)
            }
            OutlinedTextField(
                value = if(label == Constants.PASSWORD) signUpViewModel.password else signUpViewModel.confirmPassword,
                onValueChange = {
                    password -> if(label == Constants.PASSWORD) signUpViewModel.updatePassword(password)
                    else signUpViewModel.updateConfirmPassword(password)
                },
                textStyle = TextStyle(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics{
                        contentDescription = testTag
                    },
                label = { Text(text = label) },
                singleLine = true,
                shape = RoundedCornerShape(30.dp),
                visualTransformation = if(passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = {passwordVisibility = !passwordVisibility}) {
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