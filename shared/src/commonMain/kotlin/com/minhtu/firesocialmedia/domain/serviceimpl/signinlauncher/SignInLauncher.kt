package com.minhtu.firesocialmedia.domain.serviceimpl.signinlauncher

import io.mockative.Mockable

@Mockable
interface SignInLauncher {
    fun launchGoogleSignIn()
}