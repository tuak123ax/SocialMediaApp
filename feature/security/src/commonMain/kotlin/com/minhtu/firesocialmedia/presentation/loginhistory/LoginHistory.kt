package com.minhtu.firesocialmedia.presentation.loginhistory

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.utils.PasswordVerifyDialog
import com.minhtu.firesocialmedia.utils.UiUtils
import org.koin.compose.viewmodel.koinViewModel

class LoginHistory {
    companion object {
        @Composable
        fun LoginHistoryScreen(
            currentUser: UserInstance,
            loginHistoryViewModel: LoginHistoryViewModel = koinViewModel(),
            modifier: Modifier = Modifier,
            onNavigateBack: () -> Unit
        ) {
            val loginHistoryStatus by loginHistoryViewModel.loginHistoryUiState.collectAsState()
            val acknowledgeStatus by loginHistoryViewModel.acknowledgeStatus.collectAsState()
            val logoutSessionStatus by loginHistoryViewModel.logoutSessionStatus.collectAsState()

            // Password verification state for logout action
            var showLogoutPasswordDialog by remember { mutableStateOf(false) }
            var pendingLogoutSession by remember { mutableStateOf<SessionItem?>(null) }

            // Password verification state for delete history action
            var showDeletePasswordDialog by remember { mutableStateOf(false) }
            var pendingDeleteSession by remember { mutableStateOf<SessionItem?>(null) }

            LaunchedEffect(logoutSessionStatus) {
                when (logoutSessionStatus) {
                    LogoutSessionStatus.WRONG_PASSWORD -> {
                        showToast("Incorrect password. Please try again.")
                        loginHistoryViewModel.resetLogoutSessionStatus()
                    }
                    LogoutSessionStatus.ERROR -> {
                        showToast("Failed to log out session. Please try again.")
                        loginHistoryViewModel.resetLogoutSessionStatus()
                    }
                    else -> Unit
                }
            }

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
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
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
                            Text(
                                "$deviceCount Devices",
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                                SessionCard(session,
                                    onLogout = { s ->
                                        pendingLogoutSession = s
                                        showLogoutPasswordDialog = true
                                    },
                                    onDelete = { s ->
                                        pendingDeleteSession = s
                                        showDeletePasswordDialog = true
                                    }
                                )
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
                            AcknowledgeStatus.SUCCESS -> MaterialTheme.colorScheme.tertiary
                            AcknowledgeStatus.ERROR -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.error
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
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (acknowledgeStatus) {
                                AcknowledgeStatus.LOADING -> {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Saving...", color = MaterialTheme.colorScheme.onPrimary)
                                }

                                AcknowledgeStatus.SUCCESS -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Acknowledged!",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                AcknowledgeStatus.ERROR -> {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Failed. Tap to retry",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                else -> {
                                    Text(
                                        "I'm aware of all sessions",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // Password verify dialog — shown before logging out another session
            if (showLogoutPasswordDialog) {
                PasswordVerifyDialog(
                    title = "Verify Your Identity",
                    message = "Enter your password to log out this session.",
                    onConfirm = { password ->
                        showLogoutPasswordDialog = false
                        pendingLogoutSession?.let { s ->
                            loginHistoryViewModel.logoutSession(currentUser.uid, currentUser.email, password, s.sessionId)
                        }
                        pendingLogoutSession = null
                    },
                    onDismiss = {
                        showLogoutPasswordDialog = false
                        pendingLogoutSession = null
                    }
                )
            }

            // Password verify dialog — shown before deleting session history
            if (showDeletePasswordDialog) {
                PasswordVerifyDialog(
                    title = "Verify Your Identity",
                    message = "Enter your password to delete this session history.",
                    onConfirm = { password ->
                        showDeletePasswordDialog = false
                        pendingDeleteSession?.let { s ->
                            loginHistoryViewModel.deleteLoginSession(currentUser.uid, currentUser.email, password, s.sessionId)
                        }
                        pendingDeleteSession = null
                    },
                    onDismiss = {
                        showDeletePasswordDialog = false
                        pendingDeleteSession = null
                    }
                )
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
                        tint = MaterialTheme.colorScheme.error
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
        fun SessionCard(
            session: SessionItem,
            onLogout: ((SessionItem) -> Unit)? = null,
            onDelete: ((SessionItem) -> Unit)? = null
        ) {
            var menuExpanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = session.deviceName,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (session.current) {
                                Spacer(Modifier.width(6.dp))
                                CurrentBadge(text = "CURRENT")
                            } else if (session.status == "LOGOUT") {
                                Spacer(Modifier.width(6.dp))
                                CurrentBadge(text = "LOGGED OUT")
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = session.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = if (session.activeNow) {
                                "Active now"
                            } else {
                                convertTimeToDateString(session.time)
                            },
                            color = if (session.activeNow) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Session actions"
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                        if (!session.current && session.status != "LOGOUT" && onLogout != null) {
                                DropdownMenuItem(
                                    text = { Text("Log out", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onLogout(session)
                                    }
                                )
                            }

                            if (onDelete != null) {
                                DropdownMenuItem(
                                    text = { Text("Delete history") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete(session)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun CurrentBadge(text: String) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        @Composable
        fun SecureAccountCard() {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Secure account",
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Instantly terminate all sessions except this device.",
                        color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onError,
                            contentColor = MaterialTheme.colorScheme.error
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                start = Offset(translateAnim.value - 200f, 0f),
                end = Offset(translateAnim.value, 0f)
            )
        }

        @Composable
        fun SkeletonLine(width: Dp, height: Dp = 12.dp, brush: Brush) {
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