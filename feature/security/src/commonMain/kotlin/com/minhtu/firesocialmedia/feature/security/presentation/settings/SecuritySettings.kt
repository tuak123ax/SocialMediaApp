package com.minhtu.firesocialmedia.feature.security.presentation.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.Utils.Companion.toTimeAgo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class SecuritySettings {
    companion object {
        @Composable
        fun SecuritySettingsScreen(
            currentUser: UserInstance,
            paddingValues: PaddingValues,
            securitySettingsViewModel: SecuritySettingsViewModel = koinViewModel(),
            onNavigateBack: () -> Unit,
            onChangePassword: () -> Unit = {},
            onNavigateTo2FAScreen: () -> Unit = {},
            onLoginActivity: () -> Unit = {},
            onSecurityCheckup: () -> Unit = {}
        ) {
            val scope = rememberCoroutineScope()
            var twoFactorEnabled by remember { mutableStateOf(currentUser.twoFAEnabled) }
            var showDisable2FAWarning by remember { mutableStateOf(false) }
            var isDisabling by remember { mutableStateOf(false) }

            val disable2FAStatus by securitySettingsViewModel.disable2FAStatus.collectAsState()
            LaunchedEffect(disable2FAStatus) {
                if (disable2FAStatus != null) {
                    if (disable2FAStatus!!.success) {
                        showToast("Disable 2FA successfully!")
                        twoFactorEnabled = false
                    } else {
                        showToast("Cannot disable 2FA now. Please try again later!")
                    }
                    isDisabling = false
                    showDisable2FAWarning = false
                    securitySettingsViewModel.resetDisable2FAStatus()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                UiUtils.BackAndTitleAndMoreOptionsRow(
                    title = "Security Settings",
                    trailingIcon = "more_horiz",
                    isMember = false,
                    navigateBack = { onNavigateBack() }
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item { SectionTitle("Login & Security") }

                    item {
                        SecurityItem(
                            icon = Icons.Default.Key,
                            title = "Change Password",
                            subtitle = if (currentUser.lastTimeChangePassword > 0)
                                "Last changed ${currentUser.lastTimeChangePassword.toTimeAgo()}"
                            else
                                "Haven't changed password before",
                            onClick = onChangePassword
                        )
                    }

                    item {
                        SecuritySwitchItem(
                            icon = Icons.Default.Security,
                            title = "Two-Factor Authentication",
                            subtitle = "Protect your account with extra security",
                            checked = twoFactorEnabled,
                            onCheckedChange = { newValue ->
                                if (twoFactorEnabled && !newValue) {
                                    showDisable2FAWarning = true
                                } else {
                                    twoFactorEnabled = newValue
                                    scope.launch {
                                        delay(100)
                                        onNavigateTo2FAScreen()
                                    }
                                }
                            }
                        )
                    }

                    item {
                        SecurityItem(
                            icon = Icons.Default.Devices,
                            title = "Login Activity",
                            subtitle = "Check where you're currently logged in",
                            onClick = onLoginActivity
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Security Checkup")
                    }

                    item {
                        SecurityCheckupCard(
                            currentUser = currentUser,
                            onClick = onSecurityCheckup
                        )
                    }
                }
            }

            if (showDisable2FAWarning) {
                Disable2FABottomSheet(
                    isLoading = isDisabling,
                    onDismiss = { showDisable2FAWarning = false },
                    onConfirmDisable = {
                        isDisabling = true
                        securitySettingsViewModel.disable2FA(currentUser)
                    }
                )
            }
        }

        fun getScreenName(): String = "SecuritySettingsScreen"

        @Composable
        fun SectionTitle(title: String) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        @Composable
        fun SecurityItem(
            icon: ImageVector,
            title: String,
            subtitle: String? = null,
            onClick: () -> Unit
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(vertical = 12.dp)
            ) {
                IconCircle(icon)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.Medium)
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }

        @Composable
        fun SecuritySwitchItem(
            icon: ImageVector,
            title: String,
            subtitle: String,
            checked: Boolean,
            onCheckedChange: (Boolean) -> Unit
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                IconCircle(icon)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.Medium)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        @Composable
        fun IconCircle(icon: ImageVector) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(icon, contentDescription = null)
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun SecurityCheckupCard(currentUser: UserInstance, onClick: () -> Unit) {
            var showCheckup by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2D2D).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShieldMoon,
                                contentDescription = null,
                                tint = Color(0xFFFF2D2D),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Security Checkup",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Scan your account for vulnerabilities",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showCheckup = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D2D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Security Checkup",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showCheckup) {
                SecurityCheckupBottomSheet(
                    currentUser = currentUser,
                    onDismiss = { showCheckup = false }
                )
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun SecurityCheckupBottomSheet(currentUser: UserInstance, onDismiss: () -> Unit) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            var scanPhase by remember { mutableStateOf(0) }
            var visibleItems by remember { mutableStateOf(0) }

            val now = getCurrentTime()
            val thirtyDays = 30L * 24 * 60 * 60 * 1000
            val ninetyDays = 90L * 24 * 60 * 60 * 1000

            data class CheckItem(val label: String, val passed: Boolean, val points: Int)

            val checks = listOf(
                CheckItem("Two-Factor Authentication", currentUser.twoFAEnabled, 40),
                CheckItem(
                    "Password changed within 30 days",
                    currentUser.lastTimeChangePassword > 0 && (now - currentUser.lastTimeChangePassword) <= thirtyDays,
                    30
                ),
                CheckItem(
                    "Privacy policy reviewed",
                    currentUser.lastTimeReadPrivacy > 0 && (now - currentUser.lastTimeReadPrivacy) <= ninetyDays,
                    15
                ),
                CheckItem(
                    "Login sessions acknowledged",
                    currentUser.lastTimeAcknowledgedLoginHistory > 0 && (now - currentUser.lastTimeAcknowledgedLoginHistory) <= ninetyDays,
                    15
                )
            )

            val finalScore = checks.filter { it.passed }.sumOf { it.points }

            LaunchedEffect(Unit) {
                repeat(checks.size) { i ->
                    delay(700L)
                    visibleItems = i + 1
                }
                delay(400)
                scanPhase = 1
            }

            val progressTarget = if (scanPhase == 1) finalScore / 100f else 0f
            val animatedProgress by animateFloatAsState(
                targetValue = progressTarget,
                animationSpec = tween(durationMillis = 1000),
                label = "scoreProgress"
            )

            val infiniteTransition = rememberInfiniteTransition(label = "scan")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ring"
            )

            val scoreColor = when {
                finalScore >= 80 -> Color(0xFF43A047)
                finalScore >= 50 -> Color(0xFFFFA726)
                else -> Color(0xFFEF5350)
            }
            val scoreLabel = when {
                finalScore >= 80 -> "Strong"
                finalScore >= 50 -> "Moderate"
                else -> "Weak"
            }

            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
                containerColor = Color(0xFF1A1A2E),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Security Checkup",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                        if (scanPhase == 0) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(140.dp),
                                color = Color(0xFFFF2D2D),
                                strokeWidth = 6.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Icon(
                                Icons.Default.ShieldMoon,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Scanning...",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 72.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(140.dp),
                                color = scoreColor,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeWidth = 10.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                                Text(
                                    text = scoreLabel,
                                    color = scoreColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        checks.forEachIndexed { index, item ->
                            if (index < visibleItems) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (item.passed) Color(0xFF43A047) else Color(0xFFEF5350),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.label,
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "+${item.points}pts",
                                        color = if (item.passed) Color(0xFF43A047) else Color.White.copy(alpha = 0.3f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (scanPhase == 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = scoreColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Done", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Disable2FABottomSheet(
            isLoading: Boolean,
            onDismiss: () -> Unit,
            onConfirmDisable: () -> Unit
        ) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { if (!isLoading) onDismiss() },
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
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
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

                        Text(
                            text = "Disable Two-Factor Authentication?",
                            style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Disabling 2FA removes an essential layer of security. " +
                                    "Your account will become significantly more vulnerable to unauthorized access and potential data breaches.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = onConfirmDisable,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isTablet) 60.dp else 54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            if (isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onError,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Disabling...",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            } else {
                                Text(
                                    text = "Confirm Disable",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = onDismiss, enabled = !isLoading) {
                            Text(
                                text = "Keep Enabled",
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


