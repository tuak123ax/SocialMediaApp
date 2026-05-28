package com.minhtu.firesocialmedia.core.domain.entity.settings

import com.minhtu.firesocialmedia.core.domain.error.changepassword.ChangePasswordError

data class ChangePasswordState(
    val isValid : Boolean = false,
    val error : ChangePasswordError? = null
)