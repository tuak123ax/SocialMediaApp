package com.minhtu.firesocialmedia.domain

import io.mockative.Mockable

@Mockable
interface SignInLauncher {
    fun launchGoogleSignIn()
}