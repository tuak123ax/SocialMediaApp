package com.minhtu.firesocialmedia.feature.auth.presentation.signin

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.constants.UiConstants
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.exitApp
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.RouterViewModel
import com.minhtu.firesocialmedia.feature.auth.presentation.signin.SignInViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.IconAndTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.PasswordVisibilityIcon
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.SubTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.TextFieldWithLeadingIcon

class SignIn {
    companion object {
        @Composable
        fun SignInScreen(
            signInViewModel: SignInViewModel = koinViewModel(),
            loadingViewModel: LoadingViewModel,
            routerViewModel: RouterViewModel,
            modifier: Modifier,
            onNavigateToSignUpScreen: () -> Unit,
            onNavigateToHomeScreen: () -> Unit,
            onNavigateToInformationScreen: () -> Unit,
            onNavigateToForgotPasswordScreen: () -> Unit,
            onNavigateToVerifyOTP: () -> Unit
        ) {
            val focusManager = LocalFocusManager.current
            val isLoading = loadingViewModel.isLoading.collectAsState()
            val localCredentials = signInViewModel.localCredentials

            LaunchedEffect(Unit) {
                signInViewModel.checkLocalAccount()
            }
            LaunchedEffect(localCredentials.value) {
                if (localCredentials.value != null) {
                    signInViewModel.signIn(showLoading = { loadingViewModel.showLoading() })
                }
            }

            val signInStatus = signInViewModel.signInState.collectAsState()
            LaunchedEffect(signInStatus.value) {
                loadingViewModel.hideLoading()
                if (signInStatus.value.signInStatus) {
                    if (signInStatus.value.error == SignInError.AccountNotExist) {
                        onNavigateToInformationScreen()
                    } else {
                        signInViewModel.check2FAStatus()
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
                        else -> { /* Do nothing */ }
                    }
                }
                signInViewModel.resetSignInStatus()
            }

            val check2FAStatus by signInViewModel.check2FAStatus.collectAsState()
            LaunchedEffect(check2FAStatus) {
                if (check2FAStatus != null) {
                    signInViewModel.currentUser.value?.let { routerViewModel.currentUser.value = it }
                    if (check2FAStatus!!) {
                        onNavigateToVerifyOTP()
                    } else {
                        onNavigateToHomeScreen()
                    }
                    signInViewModel.resetCheck2FAStatus()
                }
            }

            QuitAlertDialog()

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = modifier.background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.Center
                ) {
                    IconAndTitle(
                        icon = "fire_chat_icon",
                        title = UiConstants.SignIn.SCREEN_TITLE,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SubTitle(
                        UiConstants.SignIn.SCREEN_SUBTITLE,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(bottom = 20.dp))
                    TextFieldWithLeadingIcon(
                        value = signInViewModel.email.collectAsState().value,
                        onValueChange = { signInViewModel.updateEmail(it) },
                        label = UiConstants.SignIn.USERNAME_LABEL,
                        testTag = TestTag.TAG_USERNAME
                    )
                    PasswordTextField(UiConstants.SignIn.PASSWORD_LABEL, signInViewModel, TestTag.TAG_PASSWORD)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MyCheckbox(signInViewModel)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = UiConstants.SignIn.FORGOT_PASSWORD_TEXT,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .clickable { onNavigateToForgotPasswordScreen() }
                                .testTag(TestTag.TAG_FORGOTPASSWORD)
                                .semantics { contentDescription = TestTag.TAG_FORGOTPASSWORD }
                        )
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            signInViewModel.signIn(showLoading = { loadingViewModel.showLoading() })
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .testTag(TestTag.TAG_BUTTON_SIGNIN)
                            .semantics { contentDescription = TestTag.TAG_BUTTON_SIGNIN }
                    ) {
                        Text(text = UiConstants.SignIn.SIGN_IN_BUTTON_TEXT)
                    }

                    SeparateTextWithDivider()

                    LoginWithGoogleButton(signInWithGoogle = { signInViewModel.signInWithGoogle() })

                    TextWithSignUp(onNavigateToSignUpScreen)
                }
                if (isLoading.value) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun LoginWithGoogleButton(signInWithGoogle: () -> Unit) {
            OutlinedButton(
                onClick = { signInWithGoogle() },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(TestTag.TAG_BUTTON_SIGNINGOOGLE)
                    .semantics { contentDescription = TestTag.TAG_BUTTON_SIGNINGOOGLE }
            ) {
                CrossPlatformIcon(
                    "google",
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    "Google",
                    Modifier.size(25.dp).padding(end = 5.dp)
                )
                Text(text = UiConstants.SignIn.SIGN_IN_WITH_GOOGLE, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        @Composable
        fun TextWithSignUp(onNavigateToSignUpScreen: () -> Unit) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = UiConstants.SignIn.SIGN_UP_QUESTION,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = UiConstants.SignIn.SIGN_UP_TEXT,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable { onNavigateToSignUpScreen() }
                        .testTag(TestTag.TAG_BUTTON_SIGNUP)
                        .semantics { contentDescription = TestTag.TAG_BUTTON_SIGNUP }
                )
            }
        }

        @Composable
        fun SeparateTextWithDivider() {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = UiConstants.SignIn.SEPARATE_TEXT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            }
        }

        @Composable
        fun QuitAlertDialog() {
            var showDialog by remember { mutableStateOf(false) }
            CommonBackHandler { showDialog = true }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(UiConstants.SignIn.ALERT_DIALOG_TITLE) },
                    text = { Text(UiConstants.SignIn.ALERT_DIALOG_MESSAGE) },
                    confirmButton = {
                        Button(onClick = { exitApp() }) { Text(UiConstants.SignIn.POSITIVE_BUTTON_TEXT) }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) { Text(UiConstants.SignIn.NEGATIVE_BUTTON_TEXT) }
                    }
                )
            }
        }

        @Composable
        fun MyCheckbox(signInViewModel: SignInViewModel) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = signInViewModel.rememberPassword.collectAsState().value,
                    onCheckedChange = { signInViewModel.updateRememberPassword(it) },
                    modifier = Modifier
                        .testTag(TestTag.TAG_REMEMBERPASSWORD)
                        .semantics { contentDescription = TestTag.TAG_REMEMBERPASSWORD }
                )
                Text(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = UiConstants.SignIn.REMEMBER_PASSWORD_TEXT,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }

        @Composable
        fun PasswordTextField(label: String, signInViewModel: SignInViewModel, testTag: String) {
            var passwordVisibility by rememberSaveable { mutableStateOf(false) }
            OutlinedTextField(
                value = signInViewModel.password.collectAsState().value,
                onValueChange = { signInViewModel.updatePassword(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics { contentDescription = testTag },
                shape = RoundedCornerShape(10.dp),
                label = { Text(text = label) },
                singleLine = true,
                textStyle = TextStyle(MaterialTheme.colorScheme.onSurface),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Default.Lock, UiConstants.SignIn.PASSWORD_LABEL) },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisibility = !passwordVisibility },
                        modifier = Modifier
                            .testTag(TestTag.TAG_SHOW_PASSWORD)
                            .semantics { contentDescription = TestTag.TAG_SHOW_PASSWORD }
                    ) {
                        PasswordVisibilityIcon(
                            passwordVisibility,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            MaterialTheme.colorScheme.background.toHex()
                        )
                    }
                }
            )
        }

        fun getScreenName(): String = UiConstants.SignIn.SCREEN_NAME
    }
}


