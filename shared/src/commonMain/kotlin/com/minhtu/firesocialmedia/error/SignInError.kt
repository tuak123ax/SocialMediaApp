package com.minhtu.firesocialmedia.error

sealed class SignInError(message: String) : Throwable(message) {
    object WrongCredentials : SignInError("Wrong credentials")
    object UserNotFound : SignInError("User not found")
    data class Unknown(val error: String) : SignInError(error)
}