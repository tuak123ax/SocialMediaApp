package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.minhtu.firesocialmedia.core.platform.PlatformUIProvider

/**
 * Implementation of PlatformUIProvider that delegates to :shared's expect/actual functions.
 */
object SharedPlatformUIProvider : PlatformUIProvider {

    override fun showToast(message: String) {
        com.minhtu.firesocialmedia.platform.showToast(message)
    }

    override fun exitApp() {
        com.minhtu.firesocialmedia.platform.exitApp()
    }

    @Composable
    override fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
        com.minhtu.firesocialmedia.platform.CommonBackHandler(enabled = enabled, onBack = onBack)
    }

    @Composable
    override fun getIconPainter(icon: String): Painter? =
        com.minhtu.firesocialmedia.platform.getIconPainter(icon)

    @Composable
    override fun getIconComposable(
        icon: String,
        bgColor: String,
        tint: String?,
        modifier: Modifier
    ): (@Composable () -> Unit)? =
        com.minhtu.firesocialmedia.platform.getIconComposable(icon, bgColor, tint, modifier)

    override fun logMessage(tag: String, message: () -> String) {
        com.minhtu.firesocialmedia.platform.logMessage(tag, message)
    }

    override fun toHex(color: Color): String = color.toHex()
}