package com.minhtu.firesocialmedia.calling.platform

import com.minhtu.firesocialmedia.ios.service.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.platform.logMessage
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private const val REMOTE_MSG_TOKENS = "tokens"
private const val REMOTE_MSG_TYPE = "type"
private const val REMOTE_MSG_DATA = "data"
private const val REMOTE_MSG_BODY = "body"
private const val KEY_FCM_PRIORITY = "priority"
private const val KEY_SESSION_ID = "session_id"
private const val KEY_CALLER_NAME = "caller_name"
private const val KEY_CALLER_ID = "caller_id"
private const val KEY_CALLER_AVATAR = "caller_avatar"
private const val KEY_CALLEE_NAME = "callee_name"
private const val KEY_CALLEE_ID = "callee_id"
private const val KEY_CALLEE_AVATAR = "callee_avatar"
private const val APP_SCRIPT_URL = "https://script.google.com/macros/s/"
private const val APP_SCRIPT_ENDPOINT = "AKfycbw4JXnBNCl-hoHi2l0_l-Ugp-9icTBWPJVR5PyKqe5o7-JJ-p26yFVpBO8kUZhxtUSzWA/exec"

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
    try {
        val body = buildJsonObject {
            putJsonObject(REMOTE_MSG_DATA) {
                put(KEY_SESSION_ID, JsonPrimitive(sessionId))
                put(KEY_CALLER_ID, JsonPrimitive(senderUid))
                put(KEY_CALLER_NAME, JsonPrimitive(senderName))
                put(KEY_CALLER_AVATAR, JsonPrimitive(senderImage))
                put(KEY_CALLEE_ID, JsonPrimitive(receiverUid))
                put(KEY_CALLEE_NAME, JsonPrimitive(receiverName))
                put(KEY_CALLEE_AVATAR, JsonPrimitive(receiverImage))
                put(REMOTE_MSG_BODY, JsonPrimitive(message))
                put(REMOTE_MSG_TYPE, JsonPrimitive(type))
            }
            putJsonArray(REMOTE_MSG_TOKENS) {
                for (token in tokenList) {
                    add(JsonPrimitive(token))
                }
            }
            put(KEY_FCM_PRIORITY, JsonPrimitive("high"))
        }
        return body.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

actual fun sendMessageToServer(request: String) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val response = KtorProvider.client.post(APP_SCRIPT_URL + APP_SCRIPT_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                logMessage("sendMessageToServer", { "Notification sent successfully" })
            } else {
                logMessage("sendMessageToServer",
                    { "Failed to send notification: ${response.status}" })
            }
        } catch (e: Exception) {
            logMessage("sendMessageToServer", { e.message.toString() })
        }
    }
}
