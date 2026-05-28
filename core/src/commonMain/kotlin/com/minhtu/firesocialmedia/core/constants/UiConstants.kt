package com.minhtu.firesocialmedia.core.constants

object UiConstants {
    object SignIn {
        const val SCREEN_TITLE = "FireSocialMedia"
        const val SCREEN_SUBTITLE = "Connect with the heat of the moment"
        const val USERNAME_LABEL = "Username"
        const val PASSWORD_LABEL = "Password"
        const val REMEMBER_PASSWORD_TEXT = "Remember me"
        const val FORGOT_PASSWORD_TEXT = "Forgot password?"
        const val SIGN_IN_WITH_GOOGLE = "Sign In With Google"
        const val SIGN_UP_QUESTION = "Don't have an account?"
        const val SIGN_UP_TEXT = "Sign up for free"
        const val SIGN_IN_BUTTON_TEXT = "Sign In →"
        const val SEPARATE_TEXT = "Or register with"
        const val ALERT_DIALOG_TITLE = "Exit App"
        const val ALERT_DIALOG_MESSAGE = "Are you sure you want to exit?"
        const val POSITIVE_BUTTON_TEXT = "Yes"
        const val NEGATIVE_BUTTON_TEXT = "No"
        const val SCREEN_NAME = "SignInScreen"
        object Error {
            const val DATA_EMPTY = "Please fill all information!"
            const val INVALID_CREDENTIALS = "Your email or password is invalid!"
            const val INVALID_EMAIL = "Your email is invalid!"
            const val INVALID_USER = "Your user is invalid!"
            const val MULTI_FACTOR = "Multi factor error happened!"
            const val NETWORK_ERROR = "Please recheck your network!"
            const val TOO_MANY_REQUESTS = "Too many requests! Slow down please!"
            const val USER_DISABLED = "User is disable!"
            const val USER_NOT_FOUND = "User is not found!"
            const val WRONG_PASSWORD = "Your password is wrong!"
            const val UNKNOWN = "Error happened!"
        }
    }
    object SignUp{
        const val SCREEN_TITLE = "FireSocialMedia"
        const val SCREEN_SUBTITLE = "Connect with the heat of the moment"
        const val USERNAME_LABEL = "Username"
        const val PASSWORD_LABEL = "Password"
        const val CONFIRM_PASSWORD_LABEL = "Confirm Password"
        const val BACK_BUTTON_TEXT = "Back"
        const val SIGNUP_BUTTON_TEXT = "Create Account"
        const val SCREEN_NAME = "SignUpScreen"
        const val SIGN_UP_QUESTION = "Already have an account?"
        const val SIGN_UP_TEXT = "Sign in"
    }
    object ForgotPassword {
        const val SCREEN_TITLE = "Forgot Password"
        const val SCREEN_SUBTITLE = "No worries, enter your email and we'll send you heat to reset it."
        const val USERNAME_LABEL = "Username"
        const val RESET_PASSWORD_BUTTON_TEXT = "Send Reset Link"
        const val BACK_TO_SIGN_IN_TEXT = "Back to Login"
        const val SCREEN_NAME = "ForgotPasswordScreen"
        object Error {
            const val EMAIL_EMPTY = "Please input your email!"
            const val EMAIL_SERVER_ERROR = "Server error happened! Please try again."
            const val EMAIL_NOT_EXISTED = "This email doesn't exist!"
        }
        const val RESET_PASSWORD_MESSAGE = "Please check your email to reset password!"
    }
}