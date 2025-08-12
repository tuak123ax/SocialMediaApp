package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.ComposeUIViewController
import com.minhtu.firesocialmedia.di.IosPlatformContext
import com.minhtu.firesocialmedia.di.PlatformContext
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.useContents
import platform.Foundation.NSDictionary
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSValue
import platform.UIKit.*

actual object MainApplication {
    @Composable
    actual fun MainApp(context: Any, platformContext : PlatformContext) {
        val controller = context as? UIViewController

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (controller != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                controller.view.endEditing(true)
                            })
                        }
                    } else Modifier
                )
        ) {
            SetUpNavigation(context, platformContext)
            ToastHost()
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

    private lateinit var scrollView: UIScrollView

    override fun viewDidLoad() {
        super.viewDidLoad()

        edgesForExtendedLayout = UIRectEdgeNone

        scrollView = UIScrollView(frame = view.bounds).apply {
            autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight

            backgroundColor = UIColor.fromHex("#FF132026") // Your app background color

            keyboardDismissMode = UIScrollViewKeyboardDismissMode.UIScrollViewKeyboardDismissModeInteractive
        }

        val composeVC = ComposeUIViewController {
            MainApplication.MainApp(this@MainAppViewController, IosPlatformContext())
        }

        addChildViewController(composeVC)
        scrollView.addSubview(composeVC.view)
        view.addSubview(scrollView)

        composeVC.view.setFrame(scrollView.bounds)
        composeVC.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        composeVC.didMoveToParentViewController(this)

        registerForKeyboardNotifications()
    }

    private fun registerForKeyboardNotifications() {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIKeyboardWillChangeFrameNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            val userInfo = notification?.userInfo ?: return@addObserverForName
            val nsDict = userInfo as? NSDictionary ?: return@addObserverForName
            val key = "UIKeyboardFrameEndUserInfoKey"
            val keyboardFrameValue = nsDict.objectForKey(key) as? NSValue ?: return@addObserverForName

            val keyboardFrame = keyboardFrameValue.CGRectValue().useContents { this }
            val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }
            val isKeyboardVisible = keyboardFrame.origin.y < screenHeight
            val bottomInset = if (isKeyboardVisible) keyboardFrame.size.height else 0.0

            scrollView.contentInset = UIEdgeInsetsMake(0.0, 0.0, bottomInset, 0.0)
            scrollView.scrollIndicatorInsets = scrollView.contentInset
        }
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        NSNotificationCenter.defaultCenter.removeObserver(this)
    }
}
