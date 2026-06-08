package com.minhtu.firesocialmedia.feature.auth.presentation.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.constants.UiConstants
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.IconAndTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.PasswordVisibilityIcon
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.SubTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.TextFieldWithLeadingIcon
import com.minhtu.firesocialmedia.feature.auth.presentation.information.InformationViewModel
import org.koin.compose.viewmodel.koinViewModel

class SignUp {
    companion object {
        @Composable
        fun SignUpScreen(
            signUpViewModel: SignUpViewModel = koinViewModel(),
            informationViewModel: InformationViewModel = koinViewModel(),
            loadingViewModel: LoadingViewModel,
            modifier: Modifier,
            onNavigateToSignInScreen: () -> Unit,
            onNavigateToInformationScreen: () -> Unit
        ) {
            val focusManager = LocalFocusManager.current
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val signUpStatus = signUpViewModel.signUpStatus.collectAsState()

            LaunchedEffect(signUpStatus.value) {
                loadingViewModel.hideLoading()
                if (signUpStatus.value.signUpStatus) {
                    informationViewModel.setPendingSignUpCredentials(
                        signUpViewModel.email,
                        signUpViewModel.password
                    )
                    onNavigateToInformationScreen()
                } else {
                    if (signUpStatus.value.message.isNotEmpty()) {
                        showToast(signUpStatus.value.message)
                    }
                }
                signUpViewModel.resetSignUpStatus()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
                    IconAndTitle(
                        icon = "fire_chat_icon",
                        title = UiConstants.SignUp.SCREEN_TITLE,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SubTitle(
                        UiConstants.SignUp.SCREEN_SUBTITLE,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(bottom = 20.dp))
                    TextFieldWithLeadingIcon(
                        value = signUpViewModel.email,
                        onValueChange = { email -> signUpViewModel.updateEmail(email) },
                        label = UiConstants.SignUp.USERNAME_LABEL,
                        testTag = TestTag.TAG_USERNAME
                    )
                    PasswordField(
                        UiConstants.SignUp.PASSWORD_LABEL,
                        signUpViewModel,
                        TestTag.TAG_PASSWORD
                    )
                    PasswordField(
                        UiConstants.SignUp.CONFIRM_PASSWORD_LABEL,
                        signUpViewModel,
                        TestTag.TAG_CONFIRMPASSWORD
                    )
                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            loadingViewModel.showLoading()
                            signUpViewModel.signUp()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.TAG_BUTTON_SIGNUP)
                            .semantics { contentDescription = TestTag.TAG_BUTTON_SIGNUP }
                    ) {
                        Text(
                            text = UiConstants.SignUp.SIGNUP_BUTTON_TEXT,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    BackToSignIn(onNavigateToSignInScreen)
                }
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun PasswordField(label: String, signUpViewModel: SignUpViewModel, testTag: String) {
            var passwordVisibility by rememberSaveable { mutableStateOf(false) }
            OutlinedTextField(
                value = if (label == UiConstants.SignUp.PASSWORD_LABEL) signUpViewModel.password else signUpViewModel.confirmPassword,
                onValueChange = { password ->
                    if (label == UiConstants.SignUp.PASSWORD_LABEL) signUpViewModel.updatePassword(password)
                    else signUpViewModel.updateConfirmPassword(password)
                },
                textStyle = TextStyle(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics { contentDescription = testTag },
                label = { Text(text = label) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, UiConstants.SignUp.PASSWORD_LABEL) },
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

        @Composable
        fun BackToSignIn(onNavigateToSignInScreen: () -> Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = UiConstants.SignUp.SIGN_UP_QUESTION,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = UiConstants.SignUp.SIGN_UP_TEXT,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable { onNavigateToSignInScreen() }
                        .testTag(TestTag.TAG_BUTTON_BACK)
                        .semantics { contentDescription = TestTag.TAG_BUTTON_BACK }
                )
            }
        }

        fun getScreenName(): String = UiConstants.SignUp.SCREEN_NAME
    }
}


