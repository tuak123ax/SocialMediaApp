package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.foundation.border
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.getIconPainter
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.BaseSettingInstance
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.SettingInstance
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings.Companion.SettingItem
import com.minhtu.firesocialmedia.utils.UiUtils


class Group {
    companion object{
        @Composable
        fun GroupScreen(
            currentUser : UserInstance,
            paddingValues : PaddingValues,
            onNavigateToCreateGroupScreen: () -> Unit,
            onNavigateToExploreGroupScreen : () -> Unit,
            onNavigateToSelectGroupScreen : () -> Unit,
            onNavigateBack : () -> Unit
        ) {
            val firstTimeUseGroup = currentUser.groups.isEmpty()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                // Top bar
                UiUtils.BackAndTitleAndMoreOptionsRow(
                    title = "Groups",
                    navigateBack = { onNavigateBack() }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    if(firstTimeUseGroup) {
                        Spacer(Modifier.height(24.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Welcome to Group",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Connect and grow with your community",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .aspectRatio(16f / 9f),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
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
                        Spacer(Modifier.height(16.dp))
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "Welcome back",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "It's good to see you again.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(32.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Click here to access your groups",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            GroupButton(
                                SettingInstance(
                                    leadingIcon = Icons.Default.Group,
                                    leadingIconTint = MaterialTheme.colorScheme.onError,
                                    leadingIconBackground = MaterialTheme.colorScheme.error,
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
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        contentDescription = "trailingIcon"
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Text(
                                text = "Or explore other groups",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
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
                                leadingIconTint = MaterialTheme.colorScheme.onPrimary,
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
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    contentDescription = "trailingIcon"
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        //Explore group button
                        GroupButton(
                            SettingInstance(
                                leadingIcon = Icons.Default.Explore,
                                leadingIconTint = MaterialTheme.colorScheme.onPrimary,
                                leadingIconBackground = MaterialTheme.colorScheme.primary,
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
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    contentDescription = "trailingIcon"
                                )
                            }
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true),
                        onClick = onClickSettingItem
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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