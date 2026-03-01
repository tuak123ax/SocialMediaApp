package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.sharedmodule.ui.theme.PolicyBody
import com.minhtu.sharedmodule.ui.theme.PolicyGrayTitle
import com.minhtu.sharedmodule.ui.theme.PolicyRed
import com.minhtu.sharedmodule.ui.theme.PolicySubText

class Privacy {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun PrivacyScreen(
            onClickBack: () -> Unit
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Privacy Policy",
                                color = PolicyRed,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onClickBack,
                                modifier = Modifier
                                    .testTag(TestTag.TAG_BUTTON_BACK_TOP_BAR)
                                    .semantics {
                                        contentDescription = TestTag.TAG_BUTTON_BACK_TOP_BAR
                                    }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PolicyRed
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        )
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    item {
                        SectionLabel("INTRODUCTION")
                        BodyText(
                            "Welcome to FireSocialMedia. Your privacy is critically important to us. " +
                                    "This policy explains how we handle your personal data when you use our platform."
                        )
                    }

                    item {
                        SectionTitle("Information We Collect")
                        BodyText(
                            "At FireSocialMedia, we collect information to provide better services to all our users. " +
                                    "This helps us personalize your experience and keep the community safe."
                        )
                    }

                    item {
                        PolicyBullet(
                            title = "Profile details and identity",
                            description = "Name, username, email, and profile photo."
                        )
                    }

                    item {
                        PolicyBullet(
                            title = "Content and communications",
                            description = "Posts, comments, and direct messages sent via the app."
                        )
                    }

                    item {
                        PolicyBullet(
                            title = "Device and log information",
                            description = "IP address, device type, and app usage patterns."
                        )
                    }

                    item {
                        SectionTitle("How We Use Your Data")
                        BodyText(
                            "We use the information we collect to operate, maintain, and improve our services. This includes:"
                        )
                    }

                    item {
                        PolicyDotItem("Personalization", "Showing you content that matches your interests.")
                    }

                    item {
                        PolicyDotItem("Security", "Detecting suspicious activity and preventing misuse.")
                    }

                    item {
                        PolicyDotItem("Communication", "Sending important updates and service notifications.")
                    }

                    item {
                        Button(
                            onClick = {
                                onClickBack()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                                .padding(10.dp)
                                .testTag(TestTag.TAG_BUTTON_BACK)
                                .semantics {
                                    contentDescription = TestTag.TAG_BUTTON_BACK
                                },
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            ),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "I understand")
                        }
                    }
                }
            }
        }


        fun getScreenName() : String {
            return "PrivacyScreen"
        }

        @Composable
        fun SectionLabel(text: String) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = PolicyGrayTitle,
                letterSpacing = 1.2.sp
            )
        }

        @Composable
        fun SectionTitle(text: String) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                color = PolicyRed,
                fontWeight = FontWeight.Bold
            )
        }

        @Composable
        fun BodyText(text: String) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = PolicyBody,
                lineHeight = 22.sp
            )
        }

        @Composable
        fun PolicyBullet(
            title: String,
            description: String
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PolicyRed,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PolicySubText
                    )
                }
            }
        }

        @Composable
        fun PolicyDotItem(
            title: String,
            description: String
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PolicyRed)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolicyBody
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PolicyBody,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(start = 18.dp)
                )
            }

        }
    }
}