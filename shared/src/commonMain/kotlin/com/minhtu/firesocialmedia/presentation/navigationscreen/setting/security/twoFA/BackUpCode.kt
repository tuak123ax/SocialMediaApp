package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils

class BackUpCode {

    companion object {
        @Composable
        fun BackupCodeScreen(
            paddingValues: PaddingValues,
            loadingViewModel: LoadingViewModel,
            backUpCodeViewModel: BackUpCodeViewModel,
            onNavigateBack: () -> Unit,
            onNavigateToVerifyBackupCodeSuccessScreen: (String) -> Unit
        ) {
            val focusManager = LocalFocusManager.current
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val backupCode by backUpCodeViewModel.backupCode.collectAsState()
            val verifyStatus by backUpCodeViewModel.verifyBackupCodeStatus.collectAsState()

            LaunchedEffect(verifyStatus) {
                verifyStatus?.let {
                    loadingViewModel.hideLoading()
                    if (it.success) {
                        onNavigateToVerifyBackupCodeSuccessScreen(it.message)
                    } else {
                        showToast("Verify backup code failed! Please try again!")
                    }
                    backUpCodeViewModel.resetVerifyBackupCodeStatus()
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                val isTablet = maxWidth > 600.dp
                val contentWidth = if (isTablet) 500.dp else maxWidth

                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)) {

                    // HEADER
                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        title = "Enter Backup Code",
                        navigateBack = onNavigateBack
                    )
                    Divider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(20.dp))
                    // CONTENT
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .width(contentWidth)
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            HeaderSection(isTablet)

                            Spacer(modifier = Modifier.height(24.dp))

                            WarningCard()

                            Spacer(modifier = Modifier.height(24.dp))

                            BackupCodeInput(
                                code = backupCode,
                                onCodeChange = backUpCodeViewModel::updateBackupCode
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TagRow()

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    // BOTTOM ACTIONS
                    BottomActions(
                        isLoading = isLoading,
                        backupCode = backupCode,
                        onVerify = {
                            focusManager.clearFocus(force = true)
                            loadingViewModel.showLoading()
                            backUpCodeViewModel.verifyBackupCode(backupCode)
                        },
                        onBack = onNavigateBack
                    )
                }
            }
        }

        fun getScreenName(): String = "BackUpCodeScreen"

        // =========================
        // UI COMPONENTS
        // =========================

        @Composable
        private fun HeaderSection(isTablet: Boolean) {
            Box(
                modifier = Modifier
                    .size(if (isTablet) 100.dp else 80.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "VERIFICATION REQUIRED",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter Backup\nAccess Code",
                style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Please input your unique 10-character emergency backup code to bypass the primary authenticator.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        @Composable
        private fun WarningCard() {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        "Security Notice: Each code can only be used once. After entry, this code will be permanently invalidated.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        @Composable
        private fun TagRow() {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                AssistChip(
                    onClick = {},
                    label = { Text("SINGLE ENTRY") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.LightGray,
                        labelColor = Color.Black
                    )
                )

                AssistChip(
                    onClick = {},
                    label = { Text("SENSITIVE") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = Color.Red
                    ),
                    border = null // remove border
                )
            }
        }

        @Composable
        private fun BottomActions(
            isLoading: Boolean,
            backupCode: String,
            onVerify: () -> Unit,
            onBack: () -> Unit
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .imePadding() // prevent keyboard overlap
            ) {

                Button(
                    onClick = onVerify,
                    enabled = backupCode.replace("-", "").length == 10 && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Verify Code")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("USING OTP")
                }
            }
        }

        // =========================
        // INPUT COMPONENT
        // =========================

        @Composable
        fun BackupCodeInput(
            code: String,
            onCodeChange: (String) -> Unit
        ) {
            val focusRequester = remember { FocusRequester() }
            val cleanCode = code.replace("-", "")

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { focusRequester.requestFocus() }
                        .focusable(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    // Group 1 (5)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            val isActive = index == cleanCode.length

                            CodeCell(
                                char = cleanCode.getOrNull(index)?.toString() ?: "",
                                isFilled = index < cleanCode.length,
                                isActive = isActive,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Group 2 (5)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            val realIndex = index + 5
                            val isActive = realIndex == cleanCode.length

                            CodeCell(
                                char = cleanCode.getOrNull(realIndex)?.toString() ?: "",
                                isFilled = realIndex < cleanCode.length,
                                isActive = isActive,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter your backup code",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // Hidden input
                BasicTextField(
                    value = cleanCode,
                    onValueChange = {
                        val filtered = it
                            .uppercase()
                            .filter { c -> c.isLetterOrDigit() }
                            .take(10)

                        // chunk by 5 instead of 4
                        onCodeChange(filtered.chunked(5).joinToString("-"))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .alpha(0f)
                        .fillMaxWidth()
                        .height(1.dp)
                )
            }
        }

        @Composable
        private fun CodeCell(
            char: String,
            isFilled: Boolean,
            isActive: Boolean,
            modifier: Modifier = Modifier
        ) {
            val borderColor = when {
                isActive -> MaterialTheme.colorScheme.primary
                isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else -> Color.LightGray
            }

            Box(
                modifier = modifier
                    .aspectRatio(1f) // responsive square
                    .border(
                        1.5.dp,
                        borderColor,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}