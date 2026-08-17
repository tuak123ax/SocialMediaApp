package com.minhtu.firesocialmedia.domain.signin

interface SignInLauncherProvider {
    fun setSignInLauncher(launcher: Any)
}

interface GoogleSignInHandler : SignInLauncherProvider {
    fun handleSignInResult(credential: Any)
    fun updateSignInStatus(success: Boolean)
    fun reset()
}
