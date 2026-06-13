package com.minhtu.firesocialmedia.feature.security.presentation.twofa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.utils.UiUtils
import org.koin.compose.viewmodel.koinViewModel

class TwoFactorEnabled {
    companion object {
        @Composable
        fun TwoFactorEnabledScreen(
            paddingValues: PaddingValues,
            backupCode: String,
            isEnable2FAFlow: Boolean,
            twoFactorEnabledViewModel: TwoFactorEnabledViewModel = koinViewModel(),
            onReturnClick: () -> Unit
        ) {
            val showBackupCode = remember { mutableStateOf(false) }

            CommonBackHandler {
                // Do nothing
            }
            LaunchedEffect(Unit) {
                //If this is login flow, store a value to know this user grant access already.
                if(!isEnable2FAFlow) {
                    twoFactorEnabledViewModel.updateVerify2FASuccess()
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val isTablet = maxWidth > 600.dp
                val horizontalPadding = if (isTablet) 64.dp else 24.dp

                Column(modifier = Modifier.fillMaxSize()) {

                    // HEADER
                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        title = if (isEnable2FAFlow) "Two Factor Enabled" else "Access Granted",
                        showBackButton = false
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(20.dp))
                    // BODY (centered + scrollable)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = horizontalPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            SecuritySuccessIcon()

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = if (isEnable2FAFlow) "Two-Factor" else "Access",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (isEnable2FAFlow) "Authentication" else "Granted",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = FontStyle.Italic
                                ),
                                textAlign = TextAlign.Center
                            )

                            if (isEnable2FAFlow) {
                                Text(
                                    text = "Enabled",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isEnable2FAFlow)
                                    "Your account is now more secure. Each time you sign in, you'll need to provide your authenticator code."
                                else
                                    "You have successfully verified your identity using a backup code and are now logged in.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = onReturnClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isEnable2FAFlow) "Return to Settings" else "Continue to Home",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(onClick = {
                                showBackupCode.value = true
                            }) {
                                Text(
                                    text = "View Backup Codes",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "SECURITY TIP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "We recommend downloading your recovery codes and storing them in a safe place. If you lose access to your device, these codes are the only way to regain entry.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Dialog overlay
                if (showBackupCode.value) {
                    BackupCodeDialog(
                        backupCode,
                        onCopy = {
                            twoFactorEnabledViewModel.copyToClipboard(backupCode)
                        },
                        onDone = {
                            showBackupCode.value = false
                        }
                    )
                }
            }
        }
        fun getScreenName() : String {
            return "TwoFactorEnabledScreen"
        }

        @Composable
        fun SecuritySuccessIcon(
            size: Dp = 80.dp,
            cornerRadius: Dp = 20.dp
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                // White filled shield
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(size * 0.6f)
                )

                // Red check on top
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(size * 0.28f)
                        .offset(y = size * 0.02f)
                )
            }
        }

        @Composable
        private fun BackupCodeDialog(
            code: String,
            onCopy: () -> Unit,
            onDone: () -> Unit
        ) {
            var isCopied by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints {
                    val cardWidth = if (maxWidth > 600.dp) 400.dp else maxWidth * 0.9f

                    Card(
                        modifier = Modifier
                            .width(cardWidth)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // Icon
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Title
                            Text(
                                text = "Your Backup Code",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Description
                            Text(
                                text = "Keep this code in a safe place. You can use it to access your account if you lose your device.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Code Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        letterSpacing = 2.sp
                                    ),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Copy Button
                            Button(
                                onClick = {
                                    onCopy()
                                    isCopied = true
                                },
                                enabled = !isCopied,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isCopied) "Copied" else "Copy Code")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Done Button
                            TextButton(onClick = onDone) {
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }
    }
}