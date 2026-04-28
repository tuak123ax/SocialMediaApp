package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA

sealed class VerifyOTPAction {
    object Enable : VerifyOTPAction()
    object Verify : VerifyOTPAction()
}