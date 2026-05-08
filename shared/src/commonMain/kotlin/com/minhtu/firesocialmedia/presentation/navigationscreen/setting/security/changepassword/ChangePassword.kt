package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.changepassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
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
    companion object {
        @Composable
        fun ChangePasswordScreen(
            paddingValues: PaddingValues,
            currentUser: UserInstance,
            changePasswordViewModel: ChangePasswordViewModel,
            loadingViewModel: LoadingViewModel,
            onNavigateToForgotPasswordScreen: () -> Unit,
            onNavigateToSignInScreen: () -> Unit,
            onNavigateBack: () -> Unit
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
                if (changePasswordState != null) {
                    loadingViewModel.hideLoading()
                    if (changePasswordState!!.isValid) {
                        showToast("Changed password successfully!")
                        onNavigateBack()
                    } else {
                        when (changePasswordState!!.error) {
                            ChangePasswordError.CurrentPasswordWrongError ->
                                showToast(ChangePasswordError.CurrentPasswordWrongError.message)
                            ChangePasswordError.DataEmptyError ->
                                showToast(ChangePasswordError.DataEmptyError.message)
                            ChangePasswordError.PasswordMismatchError ->
                                showToast(ChangePasswordError.PasswordMismatchError.message)
                            ChangePasswordError.PasswordShortError ->
                                showToast(ChangePasswordError.PasswordShortError.message)
                            ChangePasswordError.ReauthenticateRequiredError ->
                                changePasswordViewModel.retryWithReAuth(currentUser)
                            ChangePasswordError.ReauthenticateFailedError ->
                                showToast(ChangePasswordError.ReauthenticateFailedError.message)
                            ChangePasswordError.UserNotLoginError -> {
                                showToast(ChangePasswordError.UserNotLoginError.message)
                                onNavigateToSignInScreen()
                            }
                            else -> showToast("Something went wrong! Please retry later!")
                        }
                    }
                    changePasswordViewModel.resetChangePasswordState()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Dark gradient hero header ──────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                                )
                            )
                            .padding(bottom = 48.dp)
                    ) {
                        // Back button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Hero content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Change Password",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Keep your account secure",
                                color = Color.White.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // ── Scrollable content overlapping header ──────────────────
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = (-28).dp)
                    ) {
                        val isTablet = maxWidth > 600.dp
                        val horizontalPadding = if (isTablet) 48.dp else 16.dp
                        val contentMaxWidth = if (isTablet) 560.dp else maxWidth
                        val fieldHeight = if (isTablet) 68.dp else 58.dp
                        val buttonHeight = if (isTablet) 64.dp else 58.dp

                        Column(
                            modifier = Modifier
                                .widthIn(max = contentMaxWidth)
                                .align(Alignment.TopCenter)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = horizontalPadding)
                                .padding(bottom = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // ── Form card ──────────────────────────────────────
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {

                                    // Tip banner
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colorScheme.primary.copy(alpha = 0.07f))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            "Min. 8 characters with a number and symbol for more secure",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(Modifier.height(24.dp))

                                    // Current password section
                                    SectionLabel("CURRENT PASSWORD")
                                    Spacer(Modifier.height(8.dp))
                                    PasswordField(
                                        label = "",
                                        value = currentPassword,
                                        onValueChange = { changePasswordViewModel.updateCurrentPassword(it) },
                                        isVisible = showCurrent,
                                        onToggleVisibility = { showCurrent = !showCurrent },
                                        minHeight = fieldHeight
                                    )
                                    TextButton(
                                        onClick = onNavigateToForgotPasswordScreen,
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(
                                            "Forgot Password?",
                                            color = colorScheme.primary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                                    Spacer(Modifier.height(20.dp))

                                    // New password section
                                    SectionLabel("NEW PASSWORD")
                                    Spacer(Modifier.height(8.dp))
                                    PasswordField(
                                        label = "",
                                        value = newPassword,
                                        onValueChange = { changePasswordViewModel.updateNewPassword(it) },
                                        isVisible = showNew,
                                        onToggleVisibility = { showNew = !showNew },
                                        minHeight = fieldHeight
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    // Confirm password section
                                    SectionLabel("CONFIRM NEW PASSWORD")
                                    Spacer(Modifier.height(8.dp))
                                    PasswordField(
                                        label = "",
                                        value = confirmPassword,
                                        onValueChange = { changePasswordViewModel.updateConfirmPassword(it) },
                                        isVisible = showConfirm,
                                        onToggleVisibility = { showConfirm = !showConfirm },
                                        minHeight = fieldHeight
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // ── Submit button ──────────────────────────────────
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(buttonHeight)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary
                                    )
                            ) {
                                Button(
                                    onClick = {
                                        loadingViewModel.showLoading()
                                        changePasswordViewModel.updatePassword(currentUser)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Text(
                                        "Update Password",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            UiUtils.QuestionTextAndClickableText(
                                questionText = "Having trouble with security?",
                                clickableText = "Contact Support",
                                onClick = { uriHandler.openUri(Constants.SUPPORT_FACEBOOK_LINK) }
                            )
                        }
                    }
                }

                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        private fun SectionLabel(text: String) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        fun getScreenName(): String {
            return "ChangePasswordScreen"
        }
    }
}