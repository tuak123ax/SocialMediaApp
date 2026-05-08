package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.GifLoading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils

class VerifyOTP {
    companion object {
        @Composable
        fun VerifyOTPScreen(
            paddingValues: PaddingValues,
            localImageLoaderValue: ProvidedValue<*>,
            currentUser: UserInstance,
            secretKey: String,
            loadingViewModel: LoadingViewModel,
            verifyOTPViewModel: VerifyOTPViewModel,
            onNavigateToVerifyOTPSuccessScreen: (String) -> Unit = {},
            onNavigateToHomeScreen: () -> Unit,
            onNavigateToBackupCodeScreen: () -> Unit,
            onNavigateBack: () -> Unit,
            onNavigateToSignInScreen: () -> Unit
        ) {
            val focusManager = LocalFocusManager.current
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val uriHandler = LocalUriHandler.current
            val otpToVerify by verifyOTPViewModel.otpToVerify.collectAsState()
            val verifyOTPResult by verifyOTPViewModel.verifyOTPResult.collectAsState()
            val action =
                if (secretKey.isNotEmpty()) VerifyOTPAction.Enable else VerifyOTPAction.Verify
            var showCancelBottomSheet by remember { mutableStateOf(false) }
            CommonBackHandler {
                showCancelBottomSheet = true
            }
            LaunchedEffect(verifyOTPResult) {
                if (verifyOTPResult != null) {
                    loadingViewModel.hideLoading()
                    if (verifyOTPResult!!.success) {
                        logMessage("verifyOTPResult", { "Verify OTP success" })
                        when (action) {
                            VerifyOTPAction.Enable -> onNavigateToVerifyOTPSuccessScreen(
                                verifyOTPResult!!.message
                            )

                            VerifyOTPAction.Verify -> {
                                showToast("Verify OTP successfully!")
                                onNavigateToHomeScreen()
                                verifyOTPViewModel.resetOtp()
                            }
                        }
                    } else {
                        logMessage("verifyOTPResult", { verifyOTPResult!!.message })
                        showToast("OTP is incorrect. Please try again!")
                    }
                    //Reset state
                    verifyOTPViewModel.resetVerifyOTPResult()
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 140.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        title = "Verify OTP",
                        showBackButton = true,
                        navigateBack = {
                            showCancelBottomSheet = true
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(20.dp))
                    // SECURITY LABEL
                    Text(
                        text = "SECURITY LAYER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // TITLE
                    Text(
                        text = "Enter Authenticator Code",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // DESCRIPTION
                    Text(
                        text = "Please provide the 6-digit code generated by your Google Authenticator app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // OTP INPUT
                    OtpInputField(
                        code = otpToVerify,
                        onCodeChange = { verifyOTPViewModel.updateOtpToVerify(it) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (action == VerifyOTPAction.Verify) {
                        BackupCodeSection(
                            onUseBackupCode = {
                                onNavigateToBackupCodeScreen()
                            }
                        )
                    }
                }

                // FIXED BUTTON
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .imePadding()
                ) {

                    Button(
                        onClick = {
                            if (currentUser.uid.isNotEmpty()) {
                                focusManager.clearFocus(force = true)
                                loadingViewModel.showLoading()
                                if (action == VerifyOTPAction.Enable) {
                                    verifyOTPViewModel.enableOTP(
                                        currentUser,
                                        secretKey,
                                        otpToVerify
                                    )
                                } else {
                                    verifyOTPViewModel.verifyOTP(
                                        currentUser,
                                        otpToVerify
                                    )
                                }
                            } else {
                                showToast("Cannot get user information now. Please try again!")
                            }
                        },
                        enabled = otpToVerify.length == 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Verify Identity →",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Contact Support",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                uriHandler.openUri(Constants.SUPPORT_FACEBOOK_LINK)
                            }
                    )
                }
                if (isLoading) {
                    GifLoading.GifLoadingScreen(
                        localImageLoaderValue,
                        "dialga",
                        "Authenticating..."
                    )
                }
                if (showCancelBottomSheet) {
                    CancelVerifyOTPBottomSheet(
                        "Cancel OTP Verification?",
                        if (action == VerifyOTPAction.Enable) "Two-factor authentication won't be enabled on your account.\n" +
                                "Your account will remain less secure without this protection." else "For your security, you must complete verification to access your account.\n" +
                                "If you leave now, you'll be signed out and need to log in again.",
                        onDismiss = {
                            showCancelBottomSheet = false
                        },
                        onConfirmDisable = {
                            showCancelBottomSheet = false
                            when (action) {
                                VerifyOTPAction.Enable -> {
                                    onNavigateBack()
                                }

                                VerifyOTPAction.Verify -> {
                                    verifyOTPViewModel.clearAccountInLocalData()
                                    onNavigateToSignInScreen()
                                }
                            }
                        }
                    )
                }
            }
        }

        fun getScreenName(): String {
            return "VerifyOTPScreen"
        }

        @Composable
        fun OtpInputField(
            code: String,
            onCodeChange: (String) -> Unit,
            length: Int = 6
        ) {
            val focusRequester = remember { FocusRequester() }

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val spacing = 8.dp

                // Calculate responsive box width
                val boxWidth = (maxWidth - spacing * (length - 1)) / length

                BasicTextField(
                    value = code,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= length) {
                            onCodeChange(newValue)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->

                        Box {
                            // Invisible real input (fix paste position)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .alpha(0f)
                            ) {
                                innerTextField()
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(length) { index ->
                                    val digit = code.getOrNull(index)?.toString() ?: ""

                                    Column(
                                        modifier = Modifier.width(boxWidth),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = digit,
                                            style = MaterialTheme.typography.headlineMedium,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(
                                                    if (digit.isNotEmpty())
                                                        MaterialTheme.colorScheme.onBackground
                                                    else
                                                        MaterialTheme.colorScheme.outline
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }

        @Composable
        fun BackupCodeSection(
            onUseBackupCode: () -> Unit,
            modifier: Modifier = Modifier
        ) {
            BoxWithConstraints(modifier = modifier.fillMaxWidth()) {

                val isTablet = maxWidth > 600.dp
                val horizontalPadding = if (isTablet) 32.dp else 20.dp

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontalPadding)
                            .padding(vertical = if (isTablet) 28.dp else 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Lost access to your app?",
                            style = if (isTablet)
                                MaterialTheme.typography.headlineSmall
                            else
                                MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Use one of your emergency recovery codes to sign in.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        FilledTonalButton(
                            onClick = onUseBackupCode,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth(if (isTablet) 0.7f else 1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "USE BACKUP CODE",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun CancelVerifyOTPBottomSheet(
            title: String,
            message: String,
            onDismiss: () -> Unit,
            onConfirmDisable: () -> Unit
        ) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )

            ModalBottomSheet(
                onDismissRequest = {
                    onDismiss()
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isTablet = maxWidth > 600.dp

                    val contentWidth = if (isTablet) 480.dp else maxWidth
                    val horizontalPadding = if (isTablet) 32.dp else 20.dp
                    val iconSize = if (isTablet) 88.dp else 72.dp
                    val iconInnerSize = iconSize * 0.5f

                    Column(
                        modifier = Modifier
                            .width(contentWidth)
                            .align(Alignment.Center)
                            .padding(horizontal = horizontalPadding, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Warning Icon
                        Box(
                            modifier = Modifier
                                .size(iconSize)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(iconInnerSize)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Title
                        Text(
                            text = title,
                            style = if (isTablet)
                                MaterialTheme.typography.headlineSmall
                            else
                                MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Confirm Button
                        Button(
                            onClick = onConfirmDisable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isTablet) 60.dp else 54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "Confirm Cancel",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Secondary action
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(
                                text = "Continue Verify",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
