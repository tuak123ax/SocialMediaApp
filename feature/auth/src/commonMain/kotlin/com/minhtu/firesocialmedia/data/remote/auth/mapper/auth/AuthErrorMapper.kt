package com.minhtu.firesocialmedia.data.remote.auth.mapper.auth

import com.minhtu.firesocialmedia.constants.auth.Constants
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.auth.AuthException
import com.minhtu.firesocialmedia.domain.entity.forgotpassword.EmailExistResult
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.error.signup.SignUpError

fun Throwable.toSignInError(): SignInError {
    val code = (this as? AuthException)?.errorCode ?: return SignInError.Unknown(message ?: "Unknown error")
    return when (code) {
        "ERROR_INVALID_EMAIL" -> SignInError.InvalidEmail
        "ERROR_WRONG_PASSWORD" -> SignInError.WrongPassword
        "INVALID_CREDENTIALS" -> SignInError.InvalidCredentials
        "ERROR_USER_DISABLED" -> SignInError.UserDisabled
        "ERROR_USER_NOT_FOUND" -> SignInError.UserNotFound
        "INVALID_USER" -> SignInError.InvalidUser
        "TOO_MANY_REQUESTS" -> SignInError.TooManyRequests
        "NETWORK_ERROR" -> SignInError.NetworkError
        "MULTI_FACTOR" -> SignInError.MultiFactor
        else -> SignInError.Unknown(message ?: code)
    }
}

fun Throwable.toSignUpError(): SignUpError {
    val code = (this as? AuthException)?.errorCode ?: return SignUpError.Unknown(message ?: "Unknown error")
    return when (code) {
        "WEAK_PASSWORD" -> SignUpError.WeakPassword
        "ERROR_INVALID_EMAIL" -> SignUpError.InvalidEmail
        "EMAIL_ALREADY_IN_USE" -> SignUpError.EmailAlreadyInUse
        "NETWORK_ERROR" -> SignUpError.NetworkError
        else -> SignUpError.Unknown(message ?: code)
    }
}

fun Boolean.toEmailExistResult(): EmailExistResult =
    if (this) EmailExistResult(true, Constants.EMAIL_EXISTED)
    else EmailExistResult(false, Constants.EMAIL_NOT_EXISTED)
