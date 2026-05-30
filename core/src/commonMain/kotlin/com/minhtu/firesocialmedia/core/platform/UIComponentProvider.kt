package com.minhtu.firesocialmedia.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * UI component provider — allows feature modules to use shared UI components
 * without depending on :shared directly.
 */
interface UIComponentProvider {
    @Composable
    fun LoadingScreen()

    @Composable
    fun IconAndTitle(icon: String, title: String, modifier: Modifier)

    @Composable
    fun SubTitle(text: String, modifier: Modifier)

    @Composable
    fun TextFieldWithLeadingIcon(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        testTag: String
    )

    @Composable
    fun PasswordVisibilityIcon(
        visible: Boolean,
        tint: Color,
        backgroundColor: String
    )
}