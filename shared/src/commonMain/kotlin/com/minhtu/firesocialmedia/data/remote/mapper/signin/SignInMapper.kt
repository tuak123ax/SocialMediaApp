package com.minhtu.firesocialmedia.data.remote.mapper.signin

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.error.signin.SignInError

fun SignInDTO.toDomain() : SignInState {
    if(message == Constants.ACCOUNT_EXISTED) return SignInState(signInStatus, SignInError.AccountExist)
    if(message == Constants.ACCOUNT_NOT_EXISTED) return SignInState(signInStatus, SignInError.AccountNotExist)
    return SignInState(signInStatus, null)
}