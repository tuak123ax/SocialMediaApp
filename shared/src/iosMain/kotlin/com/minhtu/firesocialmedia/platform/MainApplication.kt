package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.minhtu.firesocialmedia.di.IosPlatformContext
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.ui.theme.FireSocialMediaCommonTheme
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExportObjCClass
import platform.UIKit.UIColor
import platform.UIKit.UIRectEdgeNone
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController

actual object MainApplication {
    @Composable
    actual fun MainApp(context: Any, platformContext : PlatformContext) {
        val controller = context as? UIViewController

        FireSocialMediaCommonTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SetUpNavigation(context, platformContext)
                ToastHost()
            }
        }
    }

    @Composable
    actual fun MainAppFromNotification(context: Any, platformContext: PlatformContext, sessionId: String?, callerId: String?, calleeId: String?) {
        // iOS implementation for notification handling
        // This will be implemented when notification handling is needed
    }
}


fun MainApplicationMainAppViewController(context: Any): UIViewController {
    return MainAppViewController(context)
}

@OptIn(BetaInteropApi::class)
@ExportObjCClass
class MainAppViewController(val context: Any) : UIViewController(nibName = null, bundle = null) {
    

    override fun viewDidLoad() {
        super.viewDidLoad()

        edgesForExtendedLayout = UIRectEdgeNone
        view.backgroundColor = UIColor.fromHex("#FF132026")

        val composeVC = ComposeUIViewController {
            MainApplication.MainApp(this@MainAppViewController, IosPlatformContext())
        }

        addChildViewController(composeVC)
        view.addSubview(composeVC.view)

        composeVC.view.setFrame(view.bounds)
        composeVC.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        composeVC.view.backgroundColor = UIColor.clearColor
        composeVC.didMoveToParentViewController(this)
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        // No keyboard observers to remove
    }
}
