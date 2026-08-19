package com.minhtu.firesocialmedia.group.utils

import com.minhtu.firesocialmedia.group.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.group.entity.notification.toSharedNotification
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.group.platform.createCallMessage
import com.minhtu.firesocialmedia.group.platform.sendMessageToServer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

class Utils {
    companion object {
        suspend fun saveNotification(
            notification: NotificationInstance,
            friend: UserInstance,
            saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase
        ) {
            try {
                friend.addNotification(notification)
                saveNotificationToDatabaseUseCase.invoke(
                    friend.uid,
                    ArrayList(friend.notifications.map { it.toSharedNotification() })
                )
            } catch (_: Exception) {}
        }

        fun convertToNumberString(number: Int): String {
            return if (number < 1000) {
                number.toString()
            } else if (number < 1000000) {
                (number / 1000).toString() + "K"
            } else if (number < 1000000000) {
                (number / 1000000).toString() + "M"
            } else {
                (number / 1000000000).toString() + "M"
            }
        }

        @OptIn(ExperimentalTime::class)
        fun Long.toTimeAgo(): String {
            val now = System.now().toEpochMilliseconds()
            val diff = now - this

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = days / 30
            val years = days / 365

            fun format(value: Long, unit: String): String {
                return if (value == 1L) {
                    "$value $unit ago"
                } else {
                    "$value ${unit}s ago"
                }
            }

            return when {
                seconds < 60 -> "just now"
                minutes < 60 -> format(minutes, "minute")
                hours < 24 -> format(hours, "hour")
                days < 7 -> format(days, "day")
                days < 30 -> format(weeks, "week")
                months < 12 -> format(months, "month")
                else -> format(years, "year")
            }
        }

        @OptIn(ExperimentalEncodingApi::class)
        fun decodeBase64ToBytes(b64: String): ByteArray? {
            try {
                val cleaned = b64.substringAfter(",")
                    .replace("\\s".toRegex(), "")
                val padded = when (cleaned.length % 4) {
                    2 -> "$cleaned=="
                    3 -> "$cleaned="
                    else -> cleaned
                }
                return Base64.decode(padded)
            } catch (ex: Exception) {
                return null
            }
        }

        interface BasicCallBack {
            fun onSuccess()
            fun onFailure()
        }


        // Platform-specific: stays here
        fun sendNotification(
            notiContent: String,
            sessionId: String,
            currentUser: UserInstance,
            receiver: UserInstance,
            action: String
        ) {
            val tokenList = ArrayList<String>()
            tokenList.add(receiver.token)
            sendMessageToServer(
                createCallMessage(
                    notiContent, tokenList, sessionId,
                    currentUser.uid, currentUser.name, currentUser.image,
                    receiver.uid, receiver.name, receiver.image,
                    action
                )
            )
        }
    }
}
