package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.notificationconfigs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.BaseSettingInstance
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.NotificationConfig
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings.Companion.SettingItem
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings.Companion.SoftSwitch
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.TrailingContentType
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.sharedmodule.ui.theme.memberCardColor

class NotificationConfigs {
    companion object {
        @Composable
        fun NotificationConfigsScreen(paddingValues: PaddingValues,
                                      notificationConfigsViewModel: NotificationConfigsViewModel,
                                      modifier: Modifier = Modifier,
                                      onNavigateBack : () -> Unit){
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top bar
                UiUtils.BackAndTitleAndMoreOptionsRow(
                    title = "Notification Configs",
                    trailingIcon = "more_horiz",
                    isMember = false,
                    navigateBack = {
                        onNavigateBack()
                    }
                )

                val settingsList = prepareNotificationConfigs()
                val mapStatus by notificationConfigsViewModel.notificationSettings.collectAsState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(items = settingsList) { settingPart ->
                        SettingsPartWithSwitchForNotification(
                            title = settingPart.first,
                            mapStatus,
                            listItems = settingPart.second,
                            onCheckedChange = { type, checked ->
                                notificationConfigsViewModel.updateNotification(type, checked)
                            },
                            onClickSettingItem = {
                                //Do nothing
                            }
                        )
                    }
                }
                NotificationInfoBanner()
            }
        }

        fun getScreenName() : String {
            return "NotificationConfigsScreen"
        }

        fun prepareNotificationConfigs() : ArrayList<Pair<String, List<NotificationConfig>>> {
            val settingsList = ArrayList<Pair<String, List<NotificationConfig>>>(ArrayList())
            settingsList.add(
                Pair("PUSH NOTIFICATION",
                    listOf(
                        NotificationConfig(
                            leadingIcon = Icons.Filled.Favorite,
                            leadingIconTint = Color(0xFFFF4081),
                            leadingIconBackground = Color(0xFFFFE4E9),
                            name = "Likes on Posts",
                            description = "Someone likes your posts",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.SwitchButton,
                            notificationType = NotificationType.LIKE
                        ),
                        NotificationConfig(
                            leadingIcon = Icons.Filled.ModeComment,
                            leadingIconTint = Color(0xFF1F1F1F),
                            leadingIconBackground = Color(0xFFF2F2F2),
                            name = "Comments",
                            description = "Someone comments in your posts",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.SwitchButton,
                            notificationType = NotificationType.COMMENT
                        ),
                        NotificationConfig(
                            leadingIcon = Icons.Filled.PersonAddAlt1,
                            leadingIconTint = Color.Blue,
                            leadingIconBackground = Color(0xFFEAF2FF),
                            name = "Add Friend",
                            description = "Someone makes friend with you",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.SwitchButton,
                            notificationType = NotificationType.ADD_FRIEND
                        ),
                        NotificationConfig(
                            leadingIcon = Icons.Filled.PostAdd,
                            leadingIconTint = Color.Green,
                            leadingIconBackground = Color(0xFFE8F5E9),
                            name = "Post",
                            description = "Your friends upload a post",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.SwitchButton,
                            notificationType = NotificationType.UPLOAD_NEW
                        ),
                        NotificationConfig(
                            leadingIcon = Icons.Filled.IosShare,
                            leadingIconTint = Color.Yellow,
                            leadingIconBackground = Color(0xFFFFF7D6),
                            name = "Share",
                            description = "Your friends share a post",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.SwitchButton,
                            notificationType = NotificationType.SHARE_NEW
                        ),
                        NotificationConfig(
                            leadingIcon = Icons.Default.Group,
                            leadingIconTint = Color.Cyan,
                            leadingIconBackground = Color(0xFFE6F7F8),
                            name = "Invite to Group",
                            description = "Someone invites you to a group",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.SwitchButton,
                            notificationType = NotificationType.INVITE_TO_GROUP
                        )
                    ))
            )
            return settingsList
        }

        @Composable
        fun SettingsPartWithSwitchForNotification(
            title : String,
            mapStatus : Map<NotificationType, Boolean>,
            titleColor : Color = Color.Gray,
            listItems : List<BaseSettingInstance>?,
            onCheckedChange : (NotificationType, Boolean) -> Unit,
            onClickSettingItem : (String) -> Unit
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                //Content
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = memberCardColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    listItems.orEmpty().forEach { item ->
                        SettingItem(
                            item,
                            onClickSettingItem = {
                                onClickSettingItem(it)
                            }) {
                            // trailing content
                            when(item.trailingContentType) {
                                TrailingContentType.TrailingIcon -> {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                            .background(item.trailingIconBackground)
                                    ) {
                                        Icon(
                                            imageVector = item.trailingIcon!!,
                                            tint = item.trailingIconTint,
                                            contentDescription = "trailingIcon"
                                        )
                                    }
                                }
                                TrailingContentType.SwitchButton -> {
                                    val convertItem = item as NotificationConfig
                                    mapStatus[convertItem.notificationType]?.let { checked ->
                                        SoftSwitch(
                                            checked = checked,
                                            onCheckedChange = { checked ->
                                                onCheckedChange(
                                                    convertItem.notificationType,
                                                    checked
                                                )
                                            }
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun NotificationInfoBanner(
            modifier: Modifier = Modifier,
            text: String = "These settings only affect the notifications sent to your device and do not change notifications shown within the app."
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .background(
                        color = Color(0xFFFFF4F4), // soft pink
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFE53935), // soft red
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280), // soft gray
                    lineHeight = 20.sp
                )
            }
        }

    }
}