package com.minhtu.firesocialmedia.home.platform

import com.minhtu.firesocialmedia.home.entity.deeplinks.ShareApp
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual suspend fun queryShareApps(text: String): MutableList<ShareApp> {
    // iOS doesn't expose a direct list of share targets; return empty placeholder
    return mutableListOf()
}

actual fun launchShareAppWithDeepLink(app: ShareApp, deepLink: String) {
    // Minimal attempt to open the deep link; fallback is no-op
    val url = NSURL.URLWithString(deepLink)
    if (url != null) {
        UIApplication.sharedApplication.openURL(url)
    }
}
