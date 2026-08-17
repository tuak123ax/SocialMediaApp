package com.minhtu.firesocialmedia.utils.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.profile.TestTag
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.toHex

class TitleBarUtils {
    companion object {
        @Composable
        fun BackAndTitleAndMoreOptionsRow(
            title: String,
            titleColor: Color = Color.Unspecified,
            titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
            subTitle: String = "",
            trailingIcon: String = "",
            trailingIconTint: Color = Color.Unspecified,
            showMoreOptionsMenu: Boolean = false,
            showBackButton: Boolean = true,
            isMember: Boolean = true,
            isAdmin: Boolean = false,
            iconSize: Dp = 35.dp,
            navigateBack: () -> Unit = {},
            onClickMoreOptions: () -> Unit = {},
            onDismissRequest: () -> Unit = {},
            onLeaveGroup: () -> Unit = {},
            onManageMembers: () -> Unit = {}
        ) {

            val sideSlotWidth = iconSize + 16.dp

            val resolvedTitleColor =
                if (titleColor == Color.Unspecified)
                    MaterialTheme.colorScheme.onSurface
                else titleColor

            val resolvedTrailingIconTint =
                if (trailingIconTint == Color.Unspecified)
                    MaterialTheme.colorScheme.onSurface
                else trailingIconTint

            val hasTrailingIcon =
                isMember && trailingIcon.isNotEmpty()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {

                // LEFT SLOT
                Box(
                    modifier = Modifier.width(sideSlotWidth),
                    contentAlignment = Alignment.CenterStart
                ) {

                    if (showBackButton) {

                        CrossPlatformIcon(
                            icon = "arrow_back",
                            backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(iconSize)
                                .clip(CircleShape)
                                .clickable {
                                    navigateBack()
                                }
                                .padding(4.dp)
                                .testTag(TestTag.TAG_BUTTON_BACK)
                                .semantics {
                                    contentDescription =
                                        TestTag.TAG_BUTTON_BACK
                                }
                        )
                    }
                }

                // CENTER TITLE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {

                    Text(
                        text = title,
                        color = resolvedTitleColor,
                        fontWeight = FontWeight.Bold,
                        style = titleStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    if (subTitle.isNotEmpty()) {

                        Text(
                            text = subTitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                // RIGHT SLOT
                Box(
                    modifier = Modifier.width(sideSlotWidth),
                    contentAlignment = Alignment.CenterEnd
                ) {

                    if (hasTrailingIcon) {

                        CrossPlatformIcon(
                            icon = trailingIcon,
                            backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                            contentDescription = "More Options",
                            tint = resolvedTrailingIconTint,
                            modifier = Modifier
                                .size(iconSize)
                                .clip(CircleShape)
                                .clickable {
                                    onClickMoreOptions()
                                }
                                .padding(4.dp)
                                .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                                .semantics {
                                    contentDescription =
                                        TestTag.TAG_BUTTON_MOREOPTIONS
                                }
                        )
                    }

                    if (isMember && showMoreOptionsMenu) {

                        DropdownMenuForMoreOptionsInGroup(
                            expanded = showMoreOptionsMenu,
                            isAdmin = isAdmin,
                            onLeaveGroup = onLeaveGroup,
                            onDismissRequest = onDismissRequest,
                            onManageMembers = onManageMembers
                        )
                    }
                }
            }
        }

        @Composable
        fun DropdownMenuForMoreOptionsInGroup(
            expanded: Boolean,
            isAdmin: Boolean = false,
            onLeaveGroup: () -> Unit,
            onDismissRequest: () -> Unit,
            onManageMembers: () -> Unit
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Leave group") },
                    onClick = {
                        onLeaveGroup()
                        onDismissRequest()
                    }
                )
                if (isAdmin) {
                    DropdownMenuItem(
                        text = { Text("Manage members") },
                        onClick = {
                            onManageMembers()
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}
