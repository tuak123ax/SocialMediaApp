package com.minhtu.firesocialmedia.domain.service.signinlauncher

import io.mockative.Mockable

@Mockable
interface SignInLauncher {
    fun launchGoogleSignIn()
}