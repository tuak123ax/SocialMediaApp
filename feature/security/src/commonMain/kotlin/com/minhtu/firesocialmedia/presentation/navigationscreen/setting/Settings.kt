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
import com.minhtu.firesocialmedia.constants.security.TestTag
import com.minhtu.firesocialmedia.platform.getAppVersion
import com.minhtu.firesocialmedia.presentation.settings.AccountViewModel
import com.minhtu.firesocialmedia.security.utils.UiUtils
import org.koin.compose.koinInject


class Settings {
    companion object{
        @Composable
        fun SettingsScreen(modifier: Modifier,
                           paddingValues: PaddingValues,
                           onNavigateToSignIn: () -> Unit,
                           onNavigateToProfileInformation : () -> Unit,
                           onNavigateToGroupScreen: () -> Unit,
                           onNavigateToPrivacyScreen: () -> Unit,
                           onNavigateToSecuritySettingsScreen : () -> Unit,
                           onNavigateToNotificationConfigsScreen : () -> Unit,
                           accountViewModel: AccountViewModel = koinInject()){
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
                        accountViewModel.clearAccountInStorage()
                        accountViewModel.clearLocalData()
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        @Composable
        fun prepareSettingsData() : ArrayList<Pair<String, List<SettingInstance>>> {
            val primary = MaterialTheme.colorScheme.primary
            val primaryContainer = MaterialTheme.colorScheme.primaryContainer
            val error = MaterialTheme.colorScheme.error
            val errorContainer = MaterialTheme.colorScheme.errorContainer
            val tertiary = MaterialTheme.colorScheme.tertiary
            val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
            val secondary = MaterialTheme.colorScheme.secondary
            val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
            val onSurface = MaterialTheme.colorScheme.onSurface
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            val outline = MaterialTheme.colorScheme.outline
            val settingsList = ArrayList<Pair<String, List<SettingInstance>>>(ArrayList())
            settingsList.add(
                Pair("ACCOUNT",
                    listOf(
                        SettingInstance(
                            leadingIcon = Icons.Default.Person,
                            leadingIconTint = primary,
                            leadingIconBackground = primaryContainer,
                            name = "Profile Information",
                            description = "Name, email, phone",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = outline,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        ),
                        SettingInstance(
                            leadingIcon = Icons.Default.Group,
                            leadingIconTint = error,
                            leadingIconBackground = errorContainer,
                            name = "My Groups",
                            description = "Manage your active circles",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = outline,
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
                            leadingIconTint = tertiary,
                            leadingIconBackground = tertiaryContainer,
                            name = "Privacy",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = outline,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        ),
                        SettingInstance(
                            leadingIcon = Icons.Default.Shield,
                            leadingIconTint = error,
                            leadingIconBackground = errorContainer,
                            name = "Security Settings",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = outline,
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
                            leadingIconTint = secondary,
                            leadingIconBackground = secondaryContainer,
                            name = "Notifications",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = outline,
                            trailingIconBackground = Color.Transparent,
                            trailingContentType = TrailingContentType.TrailingIcon
                        ),
                        SettingInstance(
                            leadingIcon = Icons.Default.DarkMode,
                            leadingIconTint = onSurface,
                            leadingIconBackground = surfaceVariant,
                            name = "Dark Mode",
                            trailingIcon = Icons.Default.ArrowRightAlt,
                            trailingIconTint = outline,
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
            titleColor : Color? = null,
            listItems : List<SettingInstance>? = null,
            onClickSettingItem : (String) -> Unit
        ) {
            val resolvedTitleColor = titleColor ?: MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = resolvedTitleColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                //Content
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            titleColor : Color? = null,
            listItems : List<BaseSettingInstance>?,
            onCheckedChange : (Boolean) -> Unit,
            onClickSettingItem : (String) -> Unit
        ) {
            val resolvedTitleColor = titleColor ?: MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = resolvedTitleColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                //Content
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            val resolvedLeadingIconTint = if (item.leadingIconTint == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else item.leadingIconTint
            val resolvedLeadingIconBackground = if (item.leadingIconBackground == Color.Unspecified) MaterialTheme.colorScheme.surfaceVariant else item.leadingIconBackground
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
                        .background(resolvedLeadingIconBackground)
                ) {
                    item.leadingIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            tint = resolvedLeadingIconTint,
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if(item.description.isNotEmpty()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }

    }
}




