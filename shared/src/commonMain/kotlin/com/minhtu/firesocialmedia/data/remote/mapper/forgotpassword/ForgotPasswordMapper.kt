package com.minhtu.firesocialmedia.data.remote.mapper.forgotpassword

import com.minhtu.firesocialmedia.data.remote.dto.forgotpassword.EmailExistDTO
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult

fun EmailExistDTO.toDomain() : EmailExistResult{
    return EmailExistResult(exist, message)
}