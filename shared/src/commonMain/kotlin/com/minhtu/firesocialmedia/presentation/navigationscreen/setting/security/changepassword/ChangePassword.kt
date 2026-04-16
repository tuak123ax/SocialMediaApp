package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.changepassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.PasswordField

class ChangePassword {
    companion object{
        @Composable
        fun ChangePasswordScreen(
            paddingValues: PaddingValues,
            currentUser : UserInstance,
            changePasswordViewModel: ChangePasswordViewModel,
            loadingViewModel: LoadingViewModel,
            onNavigateToForgotPasswordScreen : () -> Unit,
            onNavigateToSignInScreen : () -> Unit,
            onNavigateBack : () -> Unit
        ) {
            val uriHandler = LocalUriHandler.current
            val currentPassword by changePasswordViewModel.currentPassword.collectAsState()
            val newPassword by changePasswordViewModel.newPassword.collectAsState()
            val confirmPassword by changePasswordViewModel.confirmPassword.collectAsState()

            var showCurrent by remember { mutableStateOf(false) }
            var showNew by remember { mutableStateOf(false) }
            var showConfirm by remember { mutableStateOf(false) }

            val colorScheme = MaterialTheme.colorScheme

            val isLoading by loadingViewModel.isLoading.collectAsState()

            val changePasswordState by changePasswordViewModel.changePasswordState.collectAsState()

            LaunchedEffect(changePasswordState) {
                if(changePasswordState != null) {
                    loadingViewModel.hideLoading()
                    if(changePasswordState!!.isValid) {
                        showToast("Changed password successfully!")
                        onNavigateBack()
                    } else {
                        when(changePasswordState!!.error) {
                            ChangePasswordError.CurrentPasswordWrongError -> {
                                showToast(ChangePasswordError.CurrentPasswordWrongError.message)
                            }
                            ChangePasswordError.DataEmptyError -> {
                                showToast(ChangePasswordError.DataEmptyError.message)
                            }
                            ChangePasswordError.PasswordMismatchError -> {
                                showToast(ChangePasswordError.PasswordMismatchError.message)
                            }
                            ChangePasswordError.PasswordShortError -> {
                                showToast(ChangePasswordError.PasswordShortError.message)
                            }
                            ChangePasswordError.ReauthenticateRequiredError -> {
                                changePasswordViewModel.retryWithReAuth(currentUser)
                            }
                            ChangePasswordError.ReauthenticateFailedError -> {
                                showToast(ChangePasswordError.ReauthenticateFailedError.message)
                            }
                            ChangePasswordError.UserNotLoginError -> {
                                showToast(ChangePasswordError.UserNotLoginError.message)
                                onNavigateToSignInScreen()
                            }
                            else -> {
                                showToast("Something went wrong! Please retry later!")
                            }
                        }
                    }
                    //reset change password state
                    changePasswordViewModel.resetChangePasswordState()
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        title = "Change Password",
                        titleStyle = MaterialTheme.typography.titleLarge,
                        navigateBack = {
                            onNavigateBack()
                        }
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ACCOUNT PROTECTION",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ElevatedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = colorScheme.surface
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(80.dp)
                                        .background(colorScheme.primary, RoundedCornerShape(2.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "To maintain a secure account, ensure your password is at least 8 characters long and includes both a number and a symbol.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        PasswordField(
                            label = "CURRENT PASSWORD",
                            value = currentPassword,
                            onValueChange = {
                                changePasswordViewModel.updateCurrentPassword(it)
                            },
                            isVisible = showCurrent,
                            onToggleVisibility = { showCurrent = !showCurrent }
                        )

                        TextButton(
                            onClick = {
                                onNavigateToForgotPasswordScreen()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Forgot Password?", color = colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PasswordField(
                            label = "NEW PASSWORD",
                            value = newPassword,
                            onValueChange = {
                                changePasswordViewModel.updateNewPassword(it)
                            },
                            isVisible = showNew,
                            onToggleVisibility = { showNew = !showNew }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PasswordField(
                            label = "CONFIRM NEW PASSWORD",
                            value = confirmPassword,
                            onValueChange = {
                                changePasswordViewModel.updateConfirmPassword(it)
                            },
                            isVisible = showConfirm,
                            onToggleVisibility = { showConfirm = !showConfirm }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                loadingViewModel.showLoading()
                                changePasswordViewModel.updatePassword(
                                    currentUser
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Update Password",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        UiUtils.QuestionTextAndClickableText(
                            questionText = "Having trouble with security?",
                            clickableText = "Contact Support",
                            onClick = {
                                uriHandler.openUri(Constants.SUPPORT_FACEBOOK_LINK)
                            }
                        )
                    }
                }
                if(isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        fun getScreenName() : String {
            return "ChangePasswordScreen"
        }
    }
}