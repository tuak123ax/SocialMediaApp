package com.minhtu.firesocialmedia.group.platform

import com.minhtu.firesocialmedia.group.entity.deeplinks.ShareApp

expect suspend fun queryShareApps(text: String): MutableList<ShareApp>

expect fun launchShareAppWithDeepLink(app: ShareApp, deepLink: String)
