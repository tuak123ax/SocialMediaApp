package com.minhtu.firesocialmedia.feature.security.presentation.twofa

sealed class VerifyOTPAction {
    object Enable : VerifyOTPAction()
    object Verify : VerifyOTPAction()
}

