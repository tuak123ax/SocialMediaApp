package com.minhtu.firesocialmedia.notification.presentation.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
