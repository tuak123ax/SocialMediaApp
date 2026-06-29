package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.minhtu.firesocialmedia.core.platform.UIComponentProvider
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.utils.UiUtils

object SharedUIComponentProvider : UIComponentProvider {

    @Composable
    override fun LoadingScreen() = Loading.LoadingScreen()

    @Composable
    override fun IconAndTitle(icon: String, title: String, modifier: Modifier) =
        UiUtils.IconAndTitle(icon = icon, title = title, modifier = modifier)

    @Composable
    override fun SubTitle(text: String, modifier: Modifier) =
        UiUtils.SubTitle(subTitle = text, modifier = modifier)

    @Composable
    override fun TextFieldWithLeadingIcon(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        testTag: String
    ) = UiUtils.TextFieldWithLeadingIcon(
        value = value,
        onValueChange = onValueChange,
        label = label,
        testTag = testTag
    )

    @Composable
    override fun PasswordVisibilityIcon(
        visible: Boolean,
        tint: Color,
        backgroundColor: String
    ) = UiUtils.PasswordVisibilityIcon(
        passwordVisibility = visible,
        tint = tint,
        backgroundColor = backgroundColor
    )
}