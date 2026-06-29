package com.minhtu.firesocialmedia.data.remote.dto.signin

import kotlinx.serialization.Serializable

@Serializable
data class SignInDTO(
    val signInStatus : Boolean = false,
    val message:String = ""
)