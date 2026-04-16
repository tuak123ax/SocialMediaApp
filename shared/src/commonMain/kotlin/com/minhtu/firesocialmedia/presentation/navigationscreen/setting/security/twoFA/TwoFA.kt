package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.generateQrImage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.utils.UiUtils

class TwoFA {
    companion object {
        @Composable
        fun TwoFAScreen(
            paddingValues: PaddingValues,
            currentUser : UserInstance,
            twoFAViewModel: TwoFAViewModel,
            onContinue: (String) -> Unit,
            onNavigateBack: () -> Unit
        ) {
            val uriHandler = LocalUriHandler.current
            val scrollState = rememberScrollState()

            LaunchedEffect(Unit) {
                twoFAViewModel.generateSecretFor2FA()
            }

            val secretFor2FA by twoFAViewModel.secretFor2FA.collectAsState()
            val formattedKey = remember(secretFor2FA) {
                secretFor2FA?.chunked(4)?.joinToString(" ")
            }
            val qrBitmap = remember(secretFor2FA) {
                runCatching {
                    val otpAuth = twoFAViewModel.buildOtpAuthUrl(
                        appName = "FireSocialMedia",
                        user = currentUser.email,
                        secret = secretFor2FA
                    )
                    generateQrImage(otpAuth)
                }.getOrNull()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {

                // SCROLLABLE CONTENT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .verticalScroll(scrollState)
                        .padding(bottom = 140.dp) // space for bottom actions
                ) {

                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        title = "2FA",
                        titleStyle = MaterialTheme.typography.titleLarge,
                        navigateBack = onNavigateBack
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {

                        // Label
                        Text(
                            text = "SECURITY PROTOCOL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFB71C1C),
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Two–Factor\nAuthentication",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Scan the QR code with your authenticator app or enter the key manually.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // QR Card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF1F3F5)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // responsive instead of fixed height
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F2F2)),
                                    contentAlignment = Alignment.Center
                                ) {

                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap,
                                            contentDescription = "QR Code",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        // Fallback placeholder
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("QR", color = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Generating...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CenterFocusStrong,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "Align scanner with frame",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    text = "SECRET KEY",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF7F7F7)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(48.dp)
                                            .background(Color(0xFFD32F2F))
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if(formattedKey.isNullOrEmpty()) "ERROR" else formattedKey,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        if(!formattedKey.isNullOrEmpty()) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clickable {
                                                        if(secretFor2FA != null) {
                                                            showToast("Copied!")
                                                            twoFAViewModel.copySecret(secretFor2FA!!)
                                                        } else {
                                                            showToast("Preparing secret key...")
                                                        }
                                                    }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "If you can't scan the QR code, use this 16-digit key to manually link your account.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFE8F5E9)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Use Google Authenticator or Authy for best results.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // FIXED BOTTOM ACTIONS
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .imePadding() // keyboard safe
                ) {

                    Button(
                        onClick = {
                            if(secretFor2FA != null) {
                                onContinue(secretFor2FA!!)
                            } else {
                                showToast("Preparing secret...")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text(
                            text = "Continue to Verify →",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "I'm having trouble setting this up",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                uriHandler.openUri(Constants.SUPPORT_FACEBOOK_LINK)
                            },
                        color = Color.Gray
                    )
                }
            }
        }

        fun getScreenName() : String {
            return "TwoFAScreen"
        }
    }
}