package com.minhtu.firesocialmedia.presentation.navigationscreen.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.platform.getAppVersion
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.sharedmodule.ui.theme.memberCardColor

class Settings {
    companion object{
        @Composable
        fun SettingsScreen(modifier: Modifier,
                           paddingValues: PaddingValues,
                           homeViewModel: HomeViewModel,
                           onNavigateToSignIn: () -> Unit,
                           onNavigateToProfileInformation : () -> Unit,
                           onNavigateToGroupScreen: () -> Unit,
                           onNavigateToPrivacyScreen: () -> Unit,
                           onNavigateToSecuritySettingsScreen : () -> Unit,
                           onNavigateToNotificationConfigsScreen : () -> Unit){
            var darkMode by remember { mutableStateOf(false) }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
                val showDialog = remember { mutableStateOf(false) }
                UiUtils.LogoutBottomSheet(
                    onClickConfirm = {
                        homeViewModel.clearAccountInStorage()
                        homeViewModel.clearLocalData()
                    },
                    onNavigateToSignIn,
                    showDialog
                )
                val settingsList = prepareSettingsData()
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(items = settingsList) { settingPart ->
                        if(settingPart.first != "PREFERENCES") {
                            SettingsPart(
                                title = settingPart.first,
                                listItems = settingPart.second,
                                onClickSettingItem = { settingName ->
                                    when(settingName) {
                                        "Profile Information" -> {
                                            onNavigateToProfileInformation()
                                        }
                                        "My Groups" -> {
                                            onNavigateToGroupScreen()
                                        }
                                        "Privacy" -> {
                                            onNavigateToPrivacyScreen()
                                        }
                                        "Security Settings" -> {
                                            onNavigateToSecuritySettingsScreen()
                                        }
                                    }
                                }
                            )
                        } else {
                            //Comment out this part, will implement on next version
//                            SettingsPartWithSwitch(
//                                title = settingPart.first,
//                                darkMode,
//                                listItems = settingPart.second,
//                                onCheckedChange = {
//                                    darkMode = it
//                                },
//                                onClickSettingItem = { settingName ->
//                                    when(settingName) {
//                                        "Notifications" -> {
//                                            onNavigateToNotificationConfigsScreen()
//                                        }
//                                        "Dark Mode" -> {
//
//                                        }
//                                    }
//                                }
//                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        showDialog.value = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .testTag(TestTag.TAG_BUTTON_LOGOUT)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_LOGOUT
                        },
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    ),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Logout,
                        "Logout"
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(text = "Logout")
                }
                Text(
                    text = "FireSocialMedia v" + getAppVersion(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        fun prepareSettingsData() : ArrayList<Pair<String, List<SettingInstance>>> {
            val settingsList = ArrayList<Pair<String, List<SettingInstance>>>(ArrayList())
            settingsList.add(
                Pair("ACCOUNT",
                    listOf(
                        SettingInstance(
                            leadingIcon = Icons.Default.Person,
                            leadingIconTint = Color.Blue,
                            leadingIconBackground = Color(0xFFEAF2FF),
                            name = "Profile Information",
                            description = "Name, email, phone",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        ),
                        SettingInstance(
                            leadingIcon = Icons.Default.Group,
                            leadingIconTint = Color.Red,
                            leadingIconBackground = Color(0xFFFFEBEE),
                            name = "My Groups",
                            description = "Manage your active circles",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        )
                    ))
            )
            settingsList.add(
                Pair("PRIVACY & SECURITY",
                    listOf(
                        SettingInstance(
                            leadingIcon = Icons.Default.Lock,
                            leadingIconTint = Color.Green,
                            leadingIconBackground = Color(0xFFE8F5E9),
                            name = "Privacy",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        ),
                        SettingInstance(
                            leadingIcon = Icons.Default.Shield,
                            leadingIconTint = Color.Red,
                            leadingIconBackground = Color(0xFFFFE5D0),
                            name = "Security Settings",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        )
                    ))
            )
            settingsList.add(
                Pair(
                    "PREFERENCES",
                    listOf(
                        SettingInstance(
                            leadingIcon = Icons.Default.Notifications,
                            leadingIconTint = Color(0xFF6A5ACD),
                            leadingIconBackground = Color(0xFFE6D9FF),
                            name = "Notifications",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        ),
                        SettingInstance(
                            leadingIcon = Icons.Default.DarkMode,
                            leadingIconTint = Color(0xFF1F1F1F),
                            leadingIconBackground = Color(0xFFF2F2F2),
                            name = "Dark Mode",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = Color.LightGray,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.SwitchButton
                        )
                    )
                )
            )
            return settingsList
        }

        fun getScreenName() : String {
            return "SettingsScreen"
        }

        @Composable
        fun SettingsPart(
            title : String,
            titleColor : Color = Color.Gray,
            listItems : List<SettingInstance>?,
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
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                                    .background(item.trailingIconBackground)
                            ) {
                                item.trailingIcon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        tint = item.trailingIconTint,
                                        contentDescription = "trailingIcon"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun SettingsPartWithSwitch(
            title : String,
            status : Boolean,
            titleColor : Color = Color.Gray,
            listItems : List<BaseSettingInstance>?,
            onCheckedChange : (Boolean) -> Unit,
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
                                    val convertItem = item as SettingInstance
                                    SoftSwitch(
                                        checked = status,
                                        onCheckedChange = { checked ->
                                            onCheckedChange(
                                                checked
                                            )
                                        }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun SettingItem(item : BaseSettingInstance,
                        interactionSource : MutableInteractionSource? = null,
                        enableRipple : Boolean = true,
                        onClickSettingItem : (String) -> Unit,
                        trailingContent: @Composable (() -> Unit)? = null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clickable (
                        interactionSource = interactionSource,
                        indication = if (enableRipple) ripple(bounded = true) else null,
                        onClick = {
                            onClickSettingItem(item.name)
                        }
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(item.leadingIconSize)
                        .clip(CircleShape)
                        .background(item.leadingIconBackground)
                ) {
                    item.leadingIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            tint = item.leadingIconTint,
                            contentDescription = "leadingIcon"
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if(item.description.isNotEmpty()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                trailingContent?.invoke()
            }
        }

        @Composable
        fun SoftSwitch(
            checked: Boolean,
            onCheckedChange: (Boolean) -> Unit
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (checked) Color.Red else Color(0xFFE5E7EB)
                    )
                    .clickable { onCheckedChange(!checked) }
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .align(
                            if (checked) Alignment.CenterEnd
                            else Alignment.CenterStart
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

    }
}