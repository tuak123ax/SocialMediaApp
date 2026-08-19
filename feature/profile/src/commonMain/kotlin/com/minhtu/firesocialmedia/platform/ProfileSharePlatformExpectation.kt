package com.minhtu.firesocialmedia.profile.platform

import com.minhtu.firesocialmedia.profile.entity.deeplinks.ShareApp

expect suspend fun queryShareApps(text: String): MutableList<ShareApp>

expect fun launchShareAppWithDeepLink(app: ShareApp, deepLink: String)
