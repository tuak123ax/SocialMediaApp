package com.minhtu.firesocialmedia.group.platform

import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import com.minhtu.firesocialmedia.group.entity.deeplinks.ShareApp
import com.minhtu.firesocialmedia.platform.getAppContext

actual suspend fun queryShareApps(text: String): MutableList<ShareApp> {
    val appContext = getAppContext()
    val pm = appContext.packageManager

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    val resolveInfos = pm.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfos.map { ri ->
        ShareApp(
            name = ri.loadLabel(pm).toString(),
            packageName = ri.activityInfo.packageName,
            activityName = ri.activityInfo.name,
            icon = ri.loadIcon(pm).toBitmap()
        )
    }.toMutableList()
}

actual fun launchShareAppWithDeepLink(app: ShareApp, deepLink: String) {
    val appContext = getAppContext()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, deepLink)
        `package` = app.packageName
        setClassName(app.packageName, app.activityName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    appContext.startActivity(intent)
}
