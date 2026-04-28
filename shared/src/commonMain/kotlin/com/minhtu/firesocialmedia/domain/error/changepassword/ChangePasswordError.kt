package com.minhtu.firesocialmedia.domain.error.changepassword

sealed class ChangePasswordError(override val message : String) : Throwable() {
    object CurrentPasswordWrongError : ChangePasswordError("Current password is not correct!")
    object DataEmptyError : ChangePasswordError("Please fill all information!")
    object PasswordMismatchError : ChangePasswordError("Passwords are different!")
    object PasswordShortError : ChangePasswordError("Password is too short!")
    object UserNotLoginError : ChangePasswordError("You need to login again to perform this action!")
    object ReauthenticateRequiredError : ChangePasswordError("Reauthenticate required!")
    object ReauthenticateFailedError : ChangePasswordError("Session expired. Please re-enter your password.")
    data class Unknown(val error: String) : ChangePasswordError(error)
}