package com.minhtu.firesocialmedia.domain.entity.signin

import com.minhtu.firesocialmedia.domain.error.signin.SignInError

data class SignInState(
    val signInStatus : Boolean = false,
    val error : SignInError?
)