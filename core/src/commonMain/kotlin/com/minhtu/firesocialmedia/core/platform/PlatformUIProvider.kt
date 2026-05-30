package com.minhtu.firesocialmedia.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

/**
 * Platform UI provider interface — implemented in :shared, injected into :feature:auth screens.
 * This breaks the circular dependency: screens depend on :core only.
 */
interface PlatformUIProvider {
    fun showToast(message: String)
    fun exitApp()

    @Composable
    fun CommonBackHandler(enabled: Boolean = true, onBack: () -> Unit)

    @Composable
    fun getIconPainter(icon: String): Painter?

    @Composable
    fun getIconComposable(
        icon: String,
        bgColor: String,
        tint: String?,
        modifier: Modifier
    ): (@Composable () -> Unit)?

    fun logMessage(tag: String, message: () -> String)

    fun toHex(color: androidx.compose.ui.graphics.Color): String
}
