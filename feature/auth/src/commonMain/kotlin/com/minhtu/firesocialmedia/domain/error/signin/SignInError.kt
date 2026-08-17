package com.minhtu.firesocialmedia.domain.error.signin

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
    object DataEmpty : SignInError("Data Empty")
    object AccountExist : SignInError("Account Existed")
    object AccountNotExist : SignInError("Account Not Existed")
    data class Unknown(val error: String) : SignInError(error)
}
