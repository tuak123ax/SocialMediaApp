package com.minhtu.firesocialmedia

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExportObjCClass
import platform.UIKit.UIViewController
import platform.UIKit.*
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString

actual object MainApplication {
    @Composable
    actual fun MainApp(context: Any) {
        SetUpNavigation(context)
        ToastHost()
    }
}

fun MainApplicationMainAppViewController(context: Any): UIViewController {
    return MainAppViewController(context)
}

@OptIn(BetaInteropApi::class)
@ExportObjCClass
class MainAppViewController(val context: Any) : UIViewController(nibName = null, bundle = null) {

    private val composeVC = ComposeUIViewController {
        MainApplication.MainApp(context)
    }

    override fun viewDidLoad() {
        super.viewDidLoad()

        addChildViewController(composeVC)
        composeVC.view.setFrame(view.bounds)
        view.addSubview(composeVC.view)
        composeVC.didMoveToParentViewController(this)

        view.userInteractionEnabled = true
        composeVC.view.userInteractionEnabled = true

        val tapGesture = UITapGestureRecognizer(
            target = this,
            action = NSSelectorFromString("dismissKeyboard")
        )
        tapGesture.cancelsTouchesInView = false
        view.addGestureRecognizer(tapGesture)
    }

    @ObjCAction
    fun dismissKeyboard() {
        println("Dismiss keyboard triggered")
        view.endEditing(true)
    }

    // Correct signature for touchesBegan override in Kotlin/Native
    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        println("Touches began - dismiss keyboard")
        view.endEditing(true)
    }
}



