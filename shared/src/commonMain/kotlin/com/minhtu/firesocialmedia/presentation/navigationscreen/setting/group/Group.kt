package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.getIconPainter
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.BaseSettingInstance
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.SettingInstance
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings.Companion.SettingItem
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.sharedmodule.ui.theme.adminCardColor

class Group {
    companion object{
        @Composable
        fun GroupScreen(
            currentUser : UserInstance,
            onNavigateToCreateGroupScreen: () -> Unit,
            onNavigateToExploreGroupScreen : () -> Unit,
            onNavigateToSelectGroupScreen : () -> Unit
        ) {
            val firstTimeUseGroup = currentUser.groups.isEmpty()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if(firstTimeUseGroup) {
                    Spacer(Modifier.height(30.dp))
                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        "Welcome to Group",
                        titleColor = Color.Red,
                        titleStyle = MaterialTheme.typography.headlineLarge,
                        "Connect and grow with your community",
                        showBackButton = false,
                        showMoreOptionsMenu = false
                    )
                    Spacer(Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .padding(10.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = getIconPainter("group_background")!!,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                } else {
                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        "Welcome back",
                        titleColor = Color.Red,
                        titleStyle = MaterialTheme.typography.headlineLarge,
                        "It's good to see you again.",
                        showBackButton = false,
                        showMoreOptionsMenu = false
                    )
                    Spacer(Modifier.height(50.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Click here to access your groups",
                            color = Color.Red,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        GroupButton(
                            SettingInstance(
                                leadingIcon = Icons.Default.Group,
                                leadingIconTint = Color.White,
                                leadingIconBackground = Color.Red,
                                leadingIconSize = 50.dp,
                                name = "Select Your Group",
                                description = "Continue where you left off"
                            ),
                            modifier = Modifier
                                .testTag(TestTag.TAG_SELECT_GROUP_BUTTON)
                                .semantics {
                                    contentDescription = TestTag.TAG_SELECT_GROUP_BUTTON
                                },
                            onClickSettingItem = {
                                onNavigateToSelectGroupScreen()
                            }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                                    .background(adminCardColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    tint = Color.Red,
                                    contentDescription = "trailingIcon"
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(50.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = Color.LightGray
                        )

                        Text(
                            text = "Or explore other groups",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = Color.LightGray
                        )
                    }

                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //Create group button
                    GroupButton(
                        SettingInstance(
                            leadingIcon = Icons.Default.Add,
                            leadingIconTint = Color.White,
                            leadingIconBackground = MaterialTheme.colorScheme.primary,
                            leadingIconSize = 50.dp,
                            name = "Create Group",
                            description = "Start your own community"
                        ),
                        modifier = Modifier
                            .testTag(TestTag.TAG_CREATE_GROUP_BUTTON)
                            .semantics {
                                contentDescription = TestTag.TAG_CREATE_GROUP_BUTTON
                            },
                        onClickSettingItem = {
                            if(currentUser.groups.size < 50) {
                                onNavigateToCreateGroupScreen()
                            } else {
                                showToast("You only can join 50 groups at the same time!")
                            }
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(adminCardColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                tint = Color.Red,
                                contentDescription = "trailingIcon"
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    //Join group button
                    GroupButton(
                        SettingInstance(
                            leadingIcon = Icons.Default.Explore,
                            leadingIconTint = Color.White,
                            leadingIconBackground = Color(0xFF5C84F1),
                            leadingIconSize = 50.dp,
                            name = "Explore Group",
                            description = "Discover new interests"
                        ),
                        modifier = Modifier
                            .testTag(TestTag.TAG_FIND_GROUP_BUTTON)
                            .semantics {
                                contentDescription = TestTag.TAG_FIND_GROUP_BUTTON
                            },
                        onClickSettingItem = {
                            if(currentUser.groups.size < 50) {
                                onNavigateToExploreGroupScreen()
                            } else {
                                showToast("You only can join 50 groups at the same time!")
                            }
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(adminCardColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                tint = Color.Red,
                                contentDescription = "trailingIcon"
                            )
                        }
                    }
                }
            }
        }

        fun getScreenName() : String {
            return "GroupScreen"
        }

        @Composable
        fun GroupButton(
            settingInstance: BaseSettingInstance,
            modifier: Modifier = Modifier,
            onClickSettingItem: () -> Unit,
            trailingContent: @Composable (() -> Unit)? = null
        ) {
            val interactionSource = remember { MutableInteractionSource() }

            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true),
                        onClick = onClickSettingItem
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                SettingItem(
                    item = settingInstance,
                    interactionSource = interactionSource,
                    enableRipple = false,
                    onClickSettingItem = { _ -> onClickSettingItem() },
                    trailingContent = trailingContent
                )
            }
        }

    }
}