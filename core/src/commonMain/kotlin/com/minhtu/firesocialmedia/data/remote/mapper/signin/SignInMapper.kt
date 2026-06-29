package com.minhtu.firesocialmedia.data.remote.mapper.signin

import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError

fun SignInDTO.toDomain() : SignInState {
    if(message == SignInError.AccountExist.message) return SignInState(signInStatus, SignInError.AccountExist)
    if(message == SignInError.AccountNotExist.message) return SignInState(signInStatus, SignInError.AccountNotExist)
    return SignInState(signInStatus, null)
}