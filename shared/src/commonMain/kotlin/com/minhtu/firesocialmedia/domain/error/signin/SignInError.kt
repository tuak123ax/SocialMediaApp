package com.minhtu.firesocialmedia.domain.error.signin

import com.minhtu.firesocialmedia.constants.Constants

sealed class SignInError(message: String) : Throwable(message) {
    object InvalidEmail : SignInError("Invalid Email")
    object WrongPassword : SignInError("Wrong Password")
    object UserDisabled : SignInError("User Disabled")
    object UserNotFound : SignInError("User Not Found")
    object InvalidCredentials : SignInError("Invalid Credentials")
    object InvalidUser : SignInError("Invalid User")
    object TooManyRequests : SignInError("Too Many Requests")
    object NetworkError : SignInError("Network Error")
    object MultiFactor : SignInError("Multi Factor")
    object DataEmpty : SignInError(Constants.DATA_EMPTY)
    object AccountExist : SignInError(Constants.ACCOUNT_EXISTED)
    object AccountNotExist : SignInError(Constants.ACCOUNT_NOT_EXISTED)
    data class Unknown(val error: String) : SignInError(error)
}