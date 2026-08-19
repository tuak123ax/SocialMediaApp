package com.minhtu.firesocialmedia.domain.error.signup

sealed class SignUpError(override val message : String) : Throwable() {
    object InvalidEmail : SignUpError("Invalid email, please try again!")
    object WeakPassword : SignUpError("Password too weak, please use the stronger password!")
    object EmailAlreadyInUse : SignUpError("Email already exists, please try another email!")
    object NetworkError : SignUpError("No internet, please recheck your network!")
    object DataEmptyError : SignUpError("Please fill all information!")
    object PasswordMismatchError : SignUpError("Passwords are different!")
    object PasswordShortError : SignUpError("Password is too short!")
    data class Unknown(val error: String) : SignUpError(error)
}
