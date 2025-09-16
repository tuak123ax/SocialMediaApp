package com.minhtu.firesocialmedia.data.mapper.forgotpassword

import com.minhtu.firesocialmedia.data.dto.forgotpassword.EmailExistDTO
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult

fun EmailExistDTO.toDomain() : EmailExistResult{
    return EmailExistResult(exist, message)
}