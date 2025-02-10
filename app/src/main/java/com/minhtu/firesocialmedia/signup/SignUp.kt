package com.minhtu.firesocialmedia.signup

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel

class SignUp {
    companion object{
        @Composable
        fun SignUpScreen(
            signUpViewModel: SignUpViewModel,
            loadingViewModel: LoadingViewModel = viewModel(),
            modifier: Modifier,
            onNavigateToSignInScreen : () -> Unit,
            onNavigateToInformationScreen: ()-> Unit
        ){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val context = LocalContext.current
            val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
            //Use launched effect to observe state one time although recomposition happened
            LaunchedEffect(lifecycleOwner.value) {
                signUpViewModel.signUpStatus.observe(lifecycleOwner.value) { signUpState ->
                    loadingViewModel.hideLoading()
                    Log.e("signUpEmail", signUpViewModel.email)
                    Log.e("signUpStatus", signUpState.signUpStatus.toString() +" "+signUpState.message)
                    if (signUpState.signUpStatus) {
                        Log.e("signUp", "onNavigateToInformationScreen")
                        onNavigateToInformationScreen()
                    } else {
                        when(signUpState.message){
                            Constants.DATA_EMPTY ->  Toast.makeText(context,"Please fill all information!", Toast.LENGTH_SHORT).show()
                            Constants.PASSWORD_MISMATCH -> Toast.makeText(context,"Passwords are different!", Toast.LENGTH_SHORT).show()
                            Constants.PASSWORD_SHORT -> Toast.makeText(context,"Password is too short!",Toast.LENGTH_SHORT).show()
                            Constants.SIGNUP_FAIL ->  Toast.makeText(context, "Sign up failed. Something went wrong!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()){
                Column(modifier = modifier, verticalArrangement = Arrangement.Center){
                    //Title
                    Text(
                        text = "Sign Up",
                        color = Color.Blue,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(bottom = 30.dp))
                    //Username textfield
                    OutlinedTextField(
                        value = signUpViewModel.email,
                        onValueChange = {
                                email -> signUpViewModel.updateEmail(email)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        label = { Text(text = "Username")},
                        singleLine = true
                    )
                    //Password textfield
                    PasswordTextField(Constants.PASSWORD, signUpViewModel)
                    //Confirm password textfield
                    PasswordTextField(Constants.CONFIRM_PASSWORD, signUpViewModel)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        //Back button
                        Button(onClick = {onNavigateToSignInScreen()}) {
                            Text(text = "Back")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                        //SignUp button
                        Button(onClick = {
                            loadingViewModel.showLoading()
                            signUpViewModel.signUp() }) {
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
        fun PasswordTextField(label : String, signUpViewModel: SignUpViewModel) {
            var passwordVisibility by rememberSaveable {
                mutableStateOf(false)
            }
            OutlinedTextField(
                value = if(label == Constants.PASSWORD) signUpViewModel.password else signUpViewModel.confirmPassword,
                onValueChange = {
                    password -> if(label == Constants.PASSWORD) signUpViewModel.updatePassword(password)
                    else signUpViewModel.updateConfirmPassword(password)
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
            return "SignUpScreen"
        }
    }
}