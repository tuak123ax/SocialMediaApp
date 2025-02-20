package com.minhtu.firesocialmedia.constants

class Constants {
    companion object{
        const val USER_PATH = "users"
        const val NEWS_PATH = "news"
        const val COMMENT_PATH = "comments"
        const val LIKED_POSTS_PATH = "likedPosts"
        const val CHANNEL_ID = "NotificationID"
        const val PASSWORD = "Password"
        const val CONFIRM_PASSWORD = "Confirm Password"
        const val DATA_EMPTY = "DATA_EMPTY"
        const val PASSWORD_MISMATCH = "PASSWORD_MISMATCH"
        const val PASSWORD_SHORT = "PASSWORD_SHORT"
        const val ACCOUNT_EXISTED = "ACCOUNT_EXISTED"
        const val ACCOUNT_NOT_EXISTED = "ACCOUNT_NOT_EXISTED"
        const val LOGIN_ERROR = "LOGIN_ERROR"
        const val SIGNUP_FAIL = "SIGNUP_FAIL"
        const val DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/firechat-aa433.appspot.com/o/unknownavatar.png?alt=media&token=9a49ff27-e5fa-4813-97d4-47bd15281550"
        const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"

        const val REMOTE_MSG_DATA = "data"

        const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"

        const val FCM_URL = "https://fcm.googleapis.com/fcm/"

        const val KEY_FCM_TOKEN = "fcm_token"
        const val PRIVATE_KEY = "PRIVATE_KEY"
        const val IV = "IV"
        const val KEY_MESSAGE = "message"

        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "name"
        const val KEY_AVATAR = "avatar"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        fun getRemoteMsgHeaders(): HashMap<String, String> {
            var remoteMsgHeaders: HashMap<String, String>? = HashMap()
            remoteMsgHeaders!![REMOTE_MSG_AUTHORIZATION] =
                "key=AAAArVU2EM0:APA91bFO912dkn-eLz9VVMkhh3_a3KDV4-cIKssP-uwXlzNhKhU35XyLj83BgLr_Y9v0ysjgd5OjP0dFpT-0TXuBnJHTQics2rCNrab7bZjCpdZsqNlB4ldER11tgJuyAHKpdBZs3QPG"
            remoteMsgHeaders[REMOTE_MSG_CONTENT_TYPE] = "application/json"
            return remoteMsgHeaders
        }
    }
}