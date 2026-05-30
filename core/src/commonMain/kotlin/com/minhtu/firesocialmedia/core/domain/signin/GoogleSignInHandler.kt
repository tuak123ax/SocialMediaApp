package com.minhtu.firesocialmedia.core.domain.signin

import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState

interface SignInLauncherProvider {
    fun setSignInLauncher(launcher: Any)
}

interface GoogleSignInHandler : SignInLauncherProvider {
    fun handleSignInResult(credential: Any)
    fun updateSignInStatus(state: SignInState)
    fun reset()
}


