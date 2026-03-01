package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.utils.UiUtils

class SecuritySettings {
    companion object {
        @Composable
        fun SecuritySettingsScreen(
            onNavigateBack: () -> Unit,
            onChangePassword: () -> Unit = {},
            onLoginActivity: () -> Unit = {},
            onSecurityCheckup: () -> Unit = {}
        ) {
            var twoFactorEnabled by remember { mutableStateOf(true) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {

                // Top bar
                UiUtils.BackAndTitleAndMoreOptionsRow(
                    title = "Security Settings",
                    trailingIcon = "more_horiz",
                    isMember = false,
                    navigateBack = {
                        onNavigateBack()
                    }
                )

                Divider(color = Color(0xFFF0F0F0))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {

                    // Login & Security
                    item {
                        SectionTitle("Login & Security")
                    }

                    item {
                        SecurityItem(
                            icon = Icons.Default.Key,
                            title = "Change Password",
                            subtitle = "Last changed 3 months ago",
                            onClick = onChangePassword
                        )
                    }

                    item {
                        SecuritySwitchItem(
                            icon = Icons.Default.Security,
                            title = "Two-Factor Authentication",
                            subtitle = "Protect your account with extra security",
                            checked = twoFactorEnabled,
                            onCheckedChange = { twoFactorEnabled = it }
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

                    // Security Checkup
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Security Checkup")
                    }

                    item {
                        SecurityCheckupCard(onClick = onSecurityCheckup)
                    }
                }
            }
        }

        fun getScreenName() : String {
            return "SecuritySettingsScreen"
        }

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
                            color = Color.Gray
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray
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
                        color = Color.Gray
                    )
                }

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF3B30)
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
                    .background(Color(0xFFF3F3F3))
            ) {
                Icon(icon,
                    contentDescription = null)
            }
        }

        @Composable
        fun SecurityCheckupCard(onClick: () -> Unit) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF2D2D)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShieldMoon,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Enhance Your Privacy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your security score is 75%. Completing the checkup will help you protect your personal information.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Start Security Checkup",
                            color = Color(0xFFFF2D2D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

    }
}