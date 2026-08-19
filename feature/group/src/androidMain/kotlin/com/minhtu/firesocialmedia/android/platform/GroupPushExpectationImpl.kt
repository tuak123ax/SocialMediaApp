package com.minhtu.firesocialmedia.group.platform

import com.minhtu.firesocialmedia.android.service.serviceimpl.notification.Client
import com.minhtu.firesocialmedia.android.service.serviceimpl.notification.NotificationApiService
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val REMOTE_MSG_TOKENS = "tokens"
private const val REMOTE_MSG_TYPE = "type"
private const val REMOTE_MSG_DATA = "data"
private const val REMOTE_MSG_TITLE = "title"
private const val REMOTE_MSG_BODY = "body"
private const val KEY_FCM_PRIORITY = "priority"
private const val KEY_FCM_TOKEN = "fcm_token"
private const val KEY_USER_ID = "user_id"
private const val KEY_AVATAR = "avatar"
private const val KEY_EMAIL = "email"
private const val KEY_SESSION_ID = "session_id"
private const val KEY_CALLER_NAME = "caller_name"
private const val KEY_CALLER_ID = "caller_id"
private const val KEY_CALLER_AVATAR = "caller_avatar"
private const val KEY_CALLEE_NAME = "callee_name"
private const val KEY_CALLEE_ID = "callee_id"
private const val KEY_CALLEE_AVATAR = "callee_avatar"
private const val APP_SCRIPT_URL = "https://script.google.com/macros/s/"

actual fun createMessageForServer(
    message: String,
    tokenList: ArrayList<String>,
    senderToken: String,
    senderUid: String,
    senderImage: String,
    senderEmail: String,
    senderName: String,
    type: String
): String {
    val body = JSONObject()
    try {
        val tokens = JSONArray()
        for (token in tokenList) {
            tokens.put(token)
        }
        val data = JSONObject()
        data.put(KEY_FCM_TOKEN, senderToken)
        data.put(KEY_USER_ID, senderUid)
        data.put(KEY_AVATAR, senderImage)
        data.put(KEY_EMAIL, senderEmail)
        data.put(REMOTE_MSG_TITLE, senderName)
        data.put(REMOTE_MSG_BODY, message)
        data.put(REMOTE_MSG_TYPE, type)

        body.put(REMOTE_MSG_DATA, data)
        body.put(REMOTE_MSG_TOKENS, tokens)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return body.toString()
}

actual fun createCallMessage(
    message: String,
    tokenList: ArrayList<String>,
    sessionId: String,
    senderUid: String,
    senderName: String,
    senderImage: String,
    receiverUid: String,
    receiverName: String,
    receiverImage: String,
    type: String
): String {
    val body = JSONObject()
    try {
        val tokens = JSONArray()
        for (token in tokenList) {
            tokens.put(token)
        }
        val data = JSONObject()
        data.put(KEY_SESSION_ID, sessionId)
        data.put(KEY_CALLER_ID, senderUid)
        data.put(KEY_CALLER_NAME, senderName)
        data.put(KEY_CALLER_AVATAR, senderImage)
        data.put(KEY_CALLEE_ID, receiverUid)
        data.put(KEY_CALLEE_NAME, receiverName)
        data.put(KEY_CALLEE_AVATAR, receiverImage)
        data.put(REMOTE_MSG_BODY, message)
        data.put(REMOTE_MSG_TYPE, type)

        body.put(REMOTE_MSG_DATA, data)
        body.put(REMOTE_MSG_TOKENS, tokens)
        body.put(KEY_FCM_PRIORITY, "high")
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return body.toString()
}

actual fun sendMessageToServer(request: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = Client.getClient(APP_SCRIPT_URL)?.create(NotificationApiService::class.java)!!
                .sendToAppScript(request).execute()
            if (response.isSuccessful) {
                logMessage("sendMessageToFCM",
                    { "Notification Sent Successfully: ${response.body()}" })
            } else {
                logMessage("sendMessageToFCM",
                    { "Error: ${response.errorBody()?.string()}" })
            }
        } catch (e: Exception) {
            logMessage("sendMessageToFCM", { "Error: ${e.message}" })
        }
    }
}
