package com.minhtu.firesocialmedia.presentation.navigationscreen.setting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType

sealed class TrailingContentType() {
    object None : TrailingContentType()
    object TrailingIcon : TrailingContentType()
    object SwitchButton : TrailingContentType()
}
sealed class BaseSettingInstance {
    abstract val leadingIcon: ImageVector?
    abstract val leadingIconTint: Color
    abstract val leadingIconBackground: Color
    abstract val leadingIconSize : Dp
    abstract val name: String
    abstract val description: String
    abstract val trailingIcon: ImageVector?
    abstract val trailingIconTint: Color
    abstract val trailingIconBackground: Color
    abstract val trailingContentType: TrailingContentType
}

data class SettingInstance(
    override val leadingIcon : ImageVector? = null,
    override val leadingIconTint : Color = Color.Unspecified,
    override val leadingIconBackground : Color = Color.Unspecified,
    override val leadingIconSize: Dp = 35.dp,
    override val name : String = "",
    override val description : String = "",
    override val trailingIcon : ImageVector? = null,
    override val trailingIconTint : Color = Color.Unspecified,
    override val trailingIconBackground : Color = Color.Unspecified,
    override val trailingContentType : TrailingContentType = TrailingContentType.None,
) : BaseSettingInstance()

data class NotificationConfig(
    override val leadingIcon : ImageVector? = null,
    override val leadingIconTint : Color = Color.Unspecified,
    override val leadingIconBackground : Color = Color.Unspecified,
    override val leadingIconSize: Dp = 35.dp,
    override val name : String = "",
    override val description : String = "",
    override val trailingIcon : ImageVector? = null,
    override val trailingIconTint : Color = Color.Unspecified,
    override val trailingIconBackground : Color = Color.Unspecified,
    override val trailingContentType : TrailingContentType = TrailingContentType.None,
    val notificationType: NotificationType = NotificationType.NONE
) : BaseSettingInstance()