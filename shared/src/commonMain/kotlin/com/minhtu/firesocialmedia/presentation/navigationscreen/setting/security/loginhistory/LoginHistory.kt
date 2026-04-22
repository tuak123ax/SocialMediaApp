package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.loginhistory

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.utils.UiUtils

class LoginHistory {
    companion object {
        @Composable
        fun LoginHistoryScreen(
            currentUser: UserInstance,
            loginHistoryViewModel: LoginHistoryViewModel,
            modifier: Modifier = Modifier,
            onNavigateBack: () -> Unit
        ) {
            val loginHistoryStatus by loginHistoryViewModel.loginHistoryUiState.collectAsState()
            val acknowledgeStatus by loginHistoryViewModel.acknowledgeStatus.collectAsState()

            LaunchedEffect(acknowledgeStatus) {
                if (acknowledgeStatus == AcknowledgeStatus.SUCCESS) {
                    kotlinx.coroutines.delay(1200)
                    loginHistoryViewModel.resetAcknowledgeStatus()
                    onNavigateBack()
                }
            }

            val deviceCount = when (loginHistoryStatus) {
                is LoginHistoryUiState.Success ->
                    (loginHistoryStatus as LoginHistoryUiState.Success).data.size
                else -> 0
            }

            LaunchedEffect(Unit) {
                loginHistoryViewModel.fetchLoginHistoryList(currentUser.uid)
            }

            BoxWithConstraints(
                modifier = modifier.fillMaxSize()
            ) {
                val isTablet = maxWidth > 600.dp
                val contentMaxWidth = if (isTablet) 600.dp else Dp.Unspecified

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = contentMaxWidth)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = if (isTablet) 24.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    item {
                        UiUtils.BackAndTitleAndMoreOptionsRow(
                            title = "Login History",
                            navigateBack = onNavigateBack
                        )
                        Divider(color = Color(0xFFF0F0F0))
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                    }

                    if (deviceCount > 0) {
                        item {
                            SecurityWarningCard()
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Sessions", style = MaterialTheme.typography.titleMedium)
                            Text("$deviceCount Devices", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                    }

                    when (loginHistoryStatus) {

                        is LoginHistoryUiState.Loading -> {
                            items(5) {
                                SkeletonSessionItem()
                            }
                        }

                        is LoginHistoryUiState.Success -> {
                            val sessions =
                                (loginHistoryStatus as LoginHistoryUiState.Success).data
                                    .sortedByDescending { it.time }

                            items(sessions) { session ->
                                SessionCard(session)
                            }
                        }

                        is LoginHistoryUiState.Empty -> {
                            item {
                                Text("No sessions found!")
                            }
                        }

                        is LoginHistoryUiState.Error -> {
                            item {
                                Text("Something went wrong. Please try again!")
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                    }

                    // Comment out sign out all feature, will support on next version
//                    item {
//                        SecureAccountCard()
//                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                    }

                    item {
                        val buttonColor = when (acknowledgeStatus) {
                            AcknowledgeStatus.SUCCESS -> Color(0xFF43A047)
                            AcknowledgeStatus.ERROR   -> Color(0xFFEF5350)
                            else                      -> Color(0xFFE53935)
                        }

                        Button(
                            onClick = {
                                if (acknowledgeStatus == AcknowledgeStatus.IDLE ||
                                    acknowledgeStatus == AcknowledgeStatus.ERROR
                                ) {
                                    loginHistoryViewModel.acknowledgeLoginHistory(currentUser)
                                }
                            },
                            enabled = acknowledgeStatus != AcknowledgeStatus.LOADING,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                disabledContainerColor = buttonColor,
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (acknowledgeStatus) {
                                AcknowledgeStatus.LOADING -> {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Saving...", color = Color.White)
                                }
                                AcknowledgeStatus.SUCCESS -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Acknowledged!", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                AcknowledgeStatus.ERROR -> {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Failed. Tap to retry", color = Color.White)
                                }
                                else -> {
                                    Text("I'm aware of all sessions", color = Color.White)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        fun getScreenName(): String {
            return "LoginHistoryScreen"
        }

        @Composable
        fun SecurityWarningCard() {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text("Check your logins", fontWeight = FontWeight.SemiBold)
                        Text(
                            "We noticed new devices recently. If this wasn’t you, change your password.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        @Composable
        fun SessionCard(session: SessionItem) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(session.deviceName, fontWeight = FontWeight.SemiBold)

                            if (session.current) {
                                Spacer(Modifier.width(8.dp))
                                Badge(text = "CURRENT")
                            }
                        }

                        Text(session.location, style = MaterialTheme.typography.bodySmall)

                        if (session.activeNow) {
                            Text(
                                "Active now",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(convertTimeToDateString(session.time), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Comment out sign out feature, will support on next version
//                    if (!session.isCurrent) {
//                        TextButton(onClick = { }) {
//                            Text("Log out", color = Color.Red)
//                        }
//                    }
                }
            }
        }

        @Composable
        fun Badge(text: String) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Red.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text,
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        @Composable
        fun SecureAccountCard() {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Secure account",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Instantly terminate all sessions except this device.",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Log out all other sessions")
                    }
                }
            }
        }

        @Composable
        fun SkeletonSessionItem() {
            val shimmer = rememberShimmerBrush()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(shimmer)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {

                        SkeletonLine(width = 120.dp, brush = shimmer)

                        Spacer(Modifier.height(6.dp))

                        SkeletonLine(width = 180.dp, brush = shimmer)

                        Spacer(Modifier.height(6.dp))

                        SkeletonLine(width = 100.dp, brush = shimmer)
                    }
                }
            }
        }

        @Composable
        fun rememberShimmerBrush(): Brush {
            val transition = rememberInfiniteTransition(label = "")

            val translateAnim = transition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = ""
            )

            return Brush.linearGradient(
                colors = listOf(
                    Color.LightGray.copy(alpha = 0.6f),
                    Color.LightGray.copy(alpha = 0.2f),
                    Color.LightGray.copy(alpha = 0.6f)
                ),
                start = Offset(translateAnim.value - 200f, 0f),
                end = Offset(translateAnim.value, 0f)
            )
        }

        @Composable
        fun SkeletonLine(
            width: Dp,
            height: Dp = 12.dp,
            brush: Brush
        ) {
            Box(
                modifier = Modifier
                    .height(height)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}