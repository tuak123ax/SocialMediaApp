package com.minhtu.firesocialmedia.core.domain.entity.signin

import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError

data class SignInState(
    val signInStatus : Boolean = false,
    val error : SignInError?
)