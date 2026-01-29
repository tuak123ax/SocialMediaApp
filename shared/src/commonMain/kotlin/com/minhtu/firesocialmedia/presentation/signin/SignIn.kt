package com.minhtu.firesocialmedia.presentation.signin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.constants.UiConstants
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.exitApp
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.IconAndTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.PasswordVisibilityIcon
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.SubTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.TextFieldWithLeadingIcon
import com.minhtu.sharedmodule.ui.theme.loginBackgroundColor

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
                        SignInError.DataEmpty -> showToast(UiConstants.SignIn.Error.DATA_EMPTY)
                        SignInError.InvalidCredentials -> showToast(UiConstants.SignIn.Error.INVALID_CREDENTIALS)
                        SignInError.InvalidEmail -> showToast(UiConstants.SignIn.Error.INVALID_EMAIL)
                        SignInError.InvalidUser -> showToast(UiConstants.SignIn.Error.INVALID_USER)
                        SignInError.MultiFactor -> showToast(UiConstants.SignIn.Error.MULTI_FACTOR)
                        SignInError.NetworkError -> showToast(UiConstants.SignIn.Error.NETWORK_ERROR)
                        SignInError.TooManyRequests -> showToast(UiConstants.SignIn.Error.TOO_MANY_REQUESTS)
                        SignInError.UserDisabled -> showToast(UiConstants.SignIn.Error.USER_DISABLED)
                        SignInError.UserNotFound -> showToast(UiConstants.SignIn.Error.USER_NOT_FOUND)
                        SignInError.WrongPassword -> showToast(UiConstants.SignIn.Error.WRONG_PASSWORD)
                        is SignInError.Unknown -> showToast(UiConstants.SignIn.Error.UNKNOWN)
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
                Column(modifier = modifier
                    .background(loginBackgroundColor),
                    verticalArrangement = Arrangement.Center) {
                    //Title
                    IconAndTitle(
                        icon = "fire_chat_icon",
                        title = UiConstants.SignIn.SCREEN_TITLE,
                        modifier = Modifier.fillMaxWidth()
                    )
                    //SubTitle
                    SubTitle(
                        UiConstants.SignIn.SCREEN_SUBTITLE,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(bottom = 20.dp))
                    //Username
                    TextFieldWithLeadingIcon(
                        value = signInViewModel.email.collectAsState().value,
                        onValueChange = { text ->
                            signInViewModel.updateEmail(text)
                        },
                        label = UiConstants.SignIn.USERNAME_LABEL,
                        testTag = TestTag.TAG_USERNAME
                    )

                    //Password
                    PasswordTextField(
                        UiConstants.SignIn.PASSWORD_LABEL,
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
                        //Remember password
                        MyCheckbox(signInViewModel)
                        Spacer(modifier = Modifier.weight(1f))
                        //Forgot password
                        Text(
                            text = UiConstants.SignIn.FORGOT_PASSWORD_TEXT,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
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
                    }

                    //SignIn button
                    Button(
                        onClick = {
                            signInViewModel.signIn(
                                showLoading = { loadingViewModel.showLoading() }
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .testTag(TestTag.TAG_BUTTON_SIGNIN)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_SIGNIN
                            }) {
                        Text(text = UiConstants.SignIn.SIGN_IN_BUTTON_TEXT)
                    }

                    SeparateTextWithDivider()

                    //Google button
                    LoginWithGoogleButton(
                        signInWithGoogle = {
                            signInViewModel.signInWithGoogle()
                        }
                    )

                    TextWithSignUp(
                        onNavigateToSignUpScreen
                    )
                }
                if (isLoading.value) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun LoginWithGoogleButton(
            signInWithGoogle : () -> Unit
        ) {
            OutlinedButton(
                onClick = {
                    signInWithGoogle()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(TestTag.TAG_BUTTON_SIGNINGOOGLE)
                    .semantics {
                        contentDescription = TestTag.TAG_BUTTON_SIGNINGOOGLE
                    }
            ) {
                CrossPlatformIcon(
                    "google",
                    backgroundColor = Color.White.toHex(),
                    "Google",
                    Modifier
                        .size(25.dp)
                        .padding(end = 5.dp)
                )
                Text(text = UiConstants.SignIn.SIGN_IN_WITH_GOOGLE, color = Color.Black)
            }
        }

        @Composable
        fun TextWithSignUp(
            onNavigateToSignUpScreen: () -> Unit
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = UiConstants.SignIn.SIGN_UP_QUESTION,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = UiConstants.SignIn.SIGN_UP_TEXT,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable {
                        onNavigateToSignUpScreen()
                    }
                        .testTag(TestTag.TAG_BUTTON_SIGNUP)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_SIGNUP
                        }
                )
            }
        }


        @Composable
        fun SeparateTextWithDivider() {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color.Gray
                )

                Text(
                    text = UiConstants.SignIn.SEPARATE_TEXT,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color.Gray
                )
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
                    title = { Text(UiConstants.SignIn.ALERT_DIALOG_TITLE) },
                    text = { Text(UiConstants.SignIn.ALERT_DIALOG_MESSAGE) },
                    confirmButton = {
                        Button(onClick = { exitApp() }) {
                            Text(UiConstants.SignIn.POSITIVE_BUTTON_TEXT)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text(UiConstants.SignIn.NEGATIVE_BUTTON_TEXT)
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
                    color = Color.LightGray,
                    text = UiConstants.SignIn.REMEMBER_PASSWORD_TEXT,
                    style = MaterialTheme.typography.bodyMedium,
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                label = { Text(text = label) },
                singleLine = true,
                textStyle = TextStyle(Color.White),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        UiConstants.SignIn.PASSWORD_LABEL
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisibility = !passwordVisibility },
                        modifier = Modifier
                            .testTag(TestTag.TAG_SHOW_PASSWORD)
                            .semantics{
                                contentDescription = TestTag.TAG_SHOW_PASSWORD
                            }
                    ) {
                        PasswordVisibilityIcon(
                            passwordVisibility,
                            Color.Gray,
                            loginBackgroundColor.toHex())
                    }
                }
            )
        }

        fun getScreenName(): String{
            return UiConstants.SignIn.SCREEN_NAME
        }
    }
}