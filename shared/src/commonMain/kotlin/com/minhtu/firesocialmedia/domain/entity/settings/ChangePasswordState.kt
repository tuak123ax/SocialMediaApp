package com.minhtu.firesocialmedia.domain.entity.settings

import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError

data class ChangePasswordState(
    val isValid : Boolean = false,
    val error : ChangePasswordError? = null
)