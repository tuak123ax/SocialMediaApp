package com.minhtu.firesocialmedia.utils

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.utils.Utils as CoreUtils
import com.minhtu.firesocialmedia.platform.createCallMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer

class Utils {
    companion object {
        // Delegated to core
        fun findNewById(newId: String, listNews: ArrayList<com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance>) =
            CoreUtils.findNewById(newId, listNews)

        suspend fun saveNotification(
            notification: com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance,
            friend: UserInstance,
            saveNotificationToDatabaseUseCase: com.minhtu.firesocialmedia.core.domain.usecases.notification.SaveNotificationToDatabaseUseCase
        ) = CoreUtils.saveNotification(notification, friend, saveNotificationToDatabaseUseCase)

        suspend fun deleteNotification(
            notification: com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance,
            currentUser: UserInstance,
            deleteNotificationFromDatabaseUseCase: com.minhtu.firesocialmedia.core.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
        ) = CoreUtils.deleteNotification(notification, currentUser, deleteNotificationFromDatabaseUseCase)

        fun getCallTypeFromSdp(sdp: String?) = CoreUtils.getCallTypeFromSdp(sdp)

        fun convertToNumberString(number: Int) = CoreUtils.convertToNumberString(number)

        fun Long.toTimeAgo() = CoreUtils.run { this@toTimeAgo.toTimeAgo() }

        fun decodeBase64ToBytes(b64: String) = CoreUtils.decodeBase64ToBytes(b64)

        interface BasicCallBack : com.minhtu.firesocialmedia.core.utils.Utils.Companion.BasicCallBack {
            override fun onSuccess()
            override fun onFailure()
        }

        interface CallStatusCallBack : com.minhtu.firesocialmedia.core.utils.Utils.Companion.CallStatusCallBack {
            override fun onSuccess(status: com.minhtu.firesocialmedia.core.domain.entity.call.CallStatus)
            override fun onFailure()
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
                createCallMessage(notiContent, tokenList, sessionId, currentUser, receiver, action)
            )
        }
    }
}