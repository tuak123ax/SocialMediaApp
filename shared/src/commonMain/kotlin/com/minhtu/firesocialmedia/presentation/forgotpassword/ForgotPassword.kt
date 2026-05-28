package com.minhtu.firesocialmedia.presentation.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.constants.UiConstants
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.IconAndTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.SubTitle
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.TextFieldWithLeadingIcon
import org.koin.compose.viewmodel.koinViewModel

class ForgotPassword{
    companion object{
        @Composable
        fun ForgotPasswordScreen(
            forgotPasswordViewModel: ForgotPasswordViewModel = koinViewModel(),
            loadingViewModel: LoadingViewModel,
            modifier: Modifier = Modifier,
            onNavigateToSignInScreen:() -> Unit) {
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val emailExisted by forgotPasswordViewModel.emailExisted.collectAsState()
            val emailSent by forgotPasswordViewModel.emailSent.collectAsState()
            LaunchedEffect(emailExisted) {
                if (emailExisted != null) {
                    if (emailExisted!!.exist) {
                        forgotPasswordViewModel.sendEmailResetPassword()
                    } else {
                        loadingViewModel.hideLoading()
                        when (emailExisted!!.message) {
                            Constants.EMAIL_EMPTY -> {
                                showToast(UiConstants.ForgotPassword.Error.EMAIL_EMPTY)
                            }

                            Constants.EMAIL_SERVER_ERROR -> {
                                showToast(UiConstants.ForgotPassword.Error.EMAIL_SERVER_ERROR)
                            }

                            Constants.EMAIL_NOT_EXISTED -> {
                                showToast(UiConstants.ForgotPassword.Error.EMAIL_NOT_EXISTED)
                            }
                        }
                    }
                    forgotPasswordViewModel.resetEmailExistStatus()
                }
            }
            LaunchedEffect(emailSent) {
                if (emailSent != null) {
                    loadingViewModel.hideLoading()
                    if (emailSent!!) {
                        showToast(UiConstants.ForgotPassword.RESET_PASSWORD_MESSAGE)
                        onNavigateToSignInScreen()
                    } else {
                        showToast(UiConstants.ForgotPassword.Error.EMAIL_SERVER_ERROR)
                    }
                    forgotPasswordViewModel.updateEmail("")
                    forgotPasswordViewModel.resetEmailResetPassword()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    //Big icon
                    CrossPlatformIcon(
                        icon = "fire_chat_icon",
                        backgroundColor = MaterialTheme.colorScheme.background.toHex(),
                        modifier = Modifier
                            .size(50.dp)
                    )
                    //Title
                    IconAndTitle(
                        hasIcon = false,
                        hasTitle = true,
                        title = UiConstants.ForgotPassword.SCREEN_TITLE,
                        titleColor = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )
                    //SubTitle
                    SubTitle(
                        subTitle = UiConstants.ForgotPassword.SCREEN_SUBTITLE,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp)
                    )
                    Spacer(modifier = Modifier.padding(bottom = 20.dp))
                    //Username
                    TextFieldWithLeadingIcon(
                        value = forgotPasswordViewModel.email,
                        onValueChange = {
                            forgotPasswordViewModel.updateEmail(it)
                        },
                        label = UiConstants.SignUp.USERNAME_LABEL,
                        testTag = TestTag.TAG_USERNAME
                    )
                    //Reset button
                    Button(
                        onClick = {
                        loadingViewModel.showLoading()
                        forgotPasswordViewModel.checkIfEmailExists() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                        ) {
                        Text(text = UiConstants.ForgotPassword.RESET_PASSWORD_BUTTON_TEXT)
                    }
                    //Back to sign in
                    Text(
                        text = UiConstants.ForgotPassword.BACK_TO_SIGN_IN_TEXT,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .clickable {
                                forgotPasswordViewModel.resetEmailExistStatus()
                                forgotPasswordViewModel.resetEmailResetPassword()
                                forgotPasswordViewModel.updateEmail("")
                                onNavigateToSignInScreen()
                        }
                            .testTag(TestTag.TAG_BUTTON_BACK)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_BACK
                            }
                    )
                }
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        fun getScreenName(): String{
            return UiConstants.ForgotPassword.SCREEN_NAME
        }
    }
}