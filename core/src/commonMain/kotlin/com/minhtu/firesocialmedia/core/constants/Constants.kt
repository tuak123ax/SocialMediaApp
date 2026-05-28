package com.minhtu.firesocialmedia.core.constants

import com.minhtu.firesocialmedia.core.storage.SupabaseStorageProvider

class Constants {
    companion object{
        const val CHANNEL_ID = "NotificationID"
        const val POST_NEWS_SERVER_ERROR = "POST_AVATAR_SERVER_ERROR"
        const val POST_NEWS_EMPTY_ERROR = "POST_NEWS_EMPTY_ERROR"
        const val UPDATE_NEWS_EMPTY_ERROR = "UPDATE_NEWS_EMPTY_ERROR"
        const val ACCOUNT_EXISTED = "ACCOUNT_EXISTED"
        const val ACCOUNT_NOT_EXISTED = "ACCOUNT_NOT_EXISTED"
        const val LOGIN_ERROR = "LOGIN_ERROR"
        const val SIGNUP_FAIL = "SIGNUP_FAIL"
        const val EMAIL_EMPTY = "EMAIL_EMPTY"
        const val EMAIL_SERVER_ERROR = "EMAIL_SERVER_ERROR"
        const val EMAIL_NOT_EXISTED = "EMAIL_NOT_EXISTED"
        const val EMAIL_EXISTED = "EMAIL_EXISTED"
        const val DATA_EMPTY = "Please fill all information!"
        const val PASSWORD_MISMATCH = "Passwords are different!"
        const val PASSWORD_SHORT = "Password is too short!"

        const val SUPABASE_STORAGE_BASE_URL = "https://pcklhkkafpomfvhboini.supabase.co/storage/v1/object/public/uploads/"
        val DEFAULT_AVATAR_URL get() = SupabaseStorageProvider.DEFAULT_AVATAR_URL
        val DEFAULT_DECADE_AVATAR_URL get() = SupabaseStorageProvider.DEFAULT_DECADE_AVATAR_URL
        val DEFAULT_ARK_AVATAR_URL_FOR_GROUP get() = SupabaseStorageProvider.DEFAULT_GROUP_AVATAR_URL
        const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"

        const val REMOTE_MSG_NOTIFICATION = "notification"

        const val REMOTE_MSG_TOKENS = "tokens"
        const val REMOTE_MSG_TYPE = "type"
        const val REMOTE_MSG_DATA = "data"
        const val REMOTE_MSG_TITLE = "title"
        const val REMOTE_MSG_BODY = "body"
        const val KEY_FCM_PRIORITY = "priority"

        const val FCM_URL = "https://fcm.googleapis.com/"

        const val APP_SCRIPT_URL = "https://script.google.com/macros/s/"
        const val APP_SCRIPT_ENDPOINT = "AKfycbw4JXnBNCl-hoHi2l0_l-Ugp-9icTBWPJVR5PyKqe5o7-JJ-p26yFVpBO8kUZhxtUSzWA/exec"

        const val APP_SCRIPT_2FA_ENDPOINT = "AKfycbwv3Jg31yCw4SiOsUwm8JA0BOZHvmk6xOqTBeOHM4_Vt0O6vQpFhwFgkofxavSDr1NJ0g/exec"
        const val KEY_FCM_TOKEN = "fcm_token"

        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "name"
        const val KEY_AVATAR = "avatar"
        const val KEY_STATUS = "status"
        const val KEY_FRIENDS = "friends"
        const val KEY_FRIEND_REQUEST = "friend_request"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"

        const val KEY_SESSION_ID = "session_id"
        const val KEY_CALLER_NAME = "caller_name"
        const val KEY_CALLER_ID = "caller_id"
        const val KEY_CALLER_AVATAR = "caller_avatar"
        const val KEY_CALLEE_NAME = "callee_name"
        const val KEY_CALLEE_ID = "callee_id"
        const val KEY_CALLEE_AVATAR = "callee_avatar"
        const val FROM_NOTIFICATION = "FROM_NOTIFICATION"
        const val SUPPORT_FACEBOOK_LINK = "https://www.facebook.com/nguyen.minh.tu.311112"
        const val KEY_2FA_VERIFIED = "2fa_verified"
    }
}