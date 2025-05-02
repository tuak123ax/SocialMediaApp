package com.minhtu.firesocialmedia.constants

class Constants {
    companion object{
        val TOKEN_PATH = "token"
        const val USER_PATH = "users"
        const val NEWS_PATH = "news"
        const val COMMENT_PATH = "comments"
        const val LIKED_POSTS_PATH = "likedPosts"
        const val FRIEND_REQUESTS_PATH = "friendRequests"
        const val NOTIFICATION_PATH = "notifications"
        const val FRIENDS_PATH = "friends"
        const val LIKED_COUNT_PATH = "likeCount"
        const val COMMENT_COUNT_PATH = "commentCount"
        const val CHANNEL_ID = "NotificationID"
        const val PASSWORD = "Password"
        const val CONFIRM_PASSWORD = "Confirm Password"
        const val DATA_EMPTY = "DATA_EMPTY"
        const val POST_NEWS_SERVER_ERROR = "POST_AVATAR_SERVER_ERROR"
        const val POST_NEWS_EMPTY_ERROR = "POST_NEWS_EMPTY_ERROR"
        const val UPDATE_NEWS_EMPTY_ERROR = "UPDATE_NEWS_EMPTY_ERROR"
        const val PASSWORD_MISMATCH = "PASSWORD_MISMATCH"
        const val PASSWORD_SHORT = "PASSWORD_SHORT"
        const val ACCOUNT_EXISTED = "ACCOUNT_EXISTED"
        const val ACCOUNT_NOT_EXISTED = "ACCOUNT_NOT_EXISTED"
        const val LOGIN_ERROR = "LOGIN_ERROR"
        const val SIGNUP_FAIL = "SIGNUP_FAIL"
        const val EMAIL_EMPTY = "EMAIL_EMPTY"
        const val EMAIL_SERVER_ERROR = "EMAIL_SERVER_ERROR"
        const val EMAIL_NOT_EXISTED = "EMAIL_NOT_EXISTED"
        const val EMAIL_EXISTED = "EMAIL_EXISTED"
        const val DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/firechat-aa433.appspot.com/o/unknownavatar.png?alt=media&token=9a49ff27-e5fa-4813-97d4-47bd15281550"
        const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"

        const val REMOTE_MSG_NOTIFICATION = "notification"

        const val REMOTE_MSG_TOKENS = "tokens"
        const val REMOTE_MSG_DATA = "data"
        const val REMOTE_MSG_TITLE = "title"
        const val REMOTE_MSG_BODY = "body"

        const val FCM_URL = "https://fcm.googleapis.com/"

        const val APP_SCRIPT_URL = "https://script.google.com/macros/s/"

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
            val remoteMsgHeaders: HashMap<String, String> = HashMap()
            remoteMsgHeaders[REMOTE_MSG_AUTHORIZATION] =
                "key=AAAArVU2EM0:APA91bFO912dkn-eLz9VVMkhh3_a3KDV4-cIKssP-uwXlzNhKhU35XyLj83BgLr_Y9v0ysjgd5OjP0dFpT-0TXuBnJHTQics2rCNrab7bZjCpdZsqNlB4ldER11tgJuyAHKpdBZs3QPG"
            remoteMsgHeaders[REMOTE_MSG_CONTENT_TYPE] = "application/json"
            return remoteMsgHeaders
        }
    }
}