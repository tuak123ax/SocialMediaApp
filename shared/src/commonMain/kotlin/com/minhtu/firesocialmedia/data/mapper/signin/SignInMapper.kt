package com.minhtu.firesocialmedia.data.mapper.signin

import com.minhtu.firesocialmedia.data.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState

fun SignInDTO.toDomain() : SignInState {
    return SignInState(signInStatus, message)
}