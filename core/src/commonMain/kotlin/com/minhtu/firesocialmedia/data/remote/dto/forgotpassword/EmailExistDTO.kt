package com.minhtu.firesocialmedia.data.remote.dto.forgotpassword

import kotlinx.serialization.Serializable

@Serializable
data class EmailExistDTO(
    val exist : Boolean = false,
    val message : String = ""
)