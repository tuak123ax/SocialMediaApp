package com.minhtu.firesocialmedia.data.remote.mapper.changepassword

import com.minhtu.firesocialmedia.data.remote.service.auth.security.AuthException
import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError

fun Throwable.toChangePasswordError(): ChangePasswordError {
    val code = (this as? AuthException)?.errorCode ?: return ChangePasswordError.Unknown(message ?: "")
    return when (code) {
        "USER_NOT_LOGIN" -> ChangePasswordError.UserNotLoginError
        "REAUTHENTICATE_REQUIRED" -> ChangePasswordError.ReauthenticateRequiredError
        else -> ChangePasswordError.Unknown(message ?: code)
    }
}

