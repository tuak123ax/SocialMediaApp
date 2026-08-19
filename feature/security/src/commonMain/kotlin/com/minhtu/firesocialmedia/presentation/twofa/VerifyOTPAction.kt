package com.minhtu.firesocialmedia.presentation.twofa

sealed class VerifyOTPAction {
    object Enable : VerifyOTPAction()
    object Verify : VerifyOTPAction()
}

