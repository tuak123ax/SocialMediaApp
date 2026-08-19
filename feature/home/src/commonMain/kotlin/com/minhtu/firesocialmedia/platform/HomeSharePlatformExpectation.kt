package com.minhtu.firesocialmedia.home.platform

import com.minhtu.firesocialmedia.home.entity.deeplinks.ShareApp

expect suspend fun queryShareApps(text: String): MutableList<ShareApp>

expect fun launchShareAppWithDeepLink(app: ShareApp, deepLink: String)
