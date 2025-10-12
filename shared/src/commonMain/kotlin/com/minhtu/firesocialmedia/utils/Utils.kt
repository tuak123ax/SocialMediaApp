package com.minhtu.firesocialmedia.utils

import androidx.compose.ui.graphics.Color
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallType
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.platform.createCallMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.minhtu.firesocialmedia.presentation.calling.audiocall.CallingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Utils {
    companion object{
        fun hexToColor(hex: String): Color {
            val cleanHex = hex.removePrefix("#")
            val colorLong = when (cleanHex.length) {
                6 -> "FF$cleanHex".toLong(16) // Add alpha if missing
                8 -> cleanHex.toLong(16)
                else -> throw IllegalArgumentException("Invalid hex color: $hex")
            }
            return Color(colorLong)
        }

        fun findNewById(newId : String, listNews : ArrayList<NewsInstance>) : NewsInstance?{
            for(new in listNews){
                if(new.id == newId) {
                    return new
                }
            }
            return null
        }

        suspend fun saveNotification(
            notification: NotificationInstance,
            friend : UserInstance,
            saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase) {
            //Save notification to friend's notification list
            try{
                friend.addNotification(notification)
                saveNotificationToDatabaseUseCase.invoke(
                    friend.uid,
                    friend.notifications)
            } catch(_: Exception) {
            }
        }

        suspend fun deleteNotification(
            notification: NotificationInstance,
            currentUser: UserInstance,
            deleteNotificationFromDatabaseUseCase: DeleteNotificationFromDatabaseUseCase) {
            //Save notification to friend's notification list
            try{
                deleteNotificationFromDatabaseUseCase.invoke(
                    currentUser.uid,
                    notification)
            } catch(_: Exception) {
            }
        }

        fun getCallTypeFromSdp(sdp: String?): CallType {
            return when {
                sdp == null -> CallType.UNKNOWN
                sdp.contains("m=video") -> CallType.VIDEO
                sdp.contains("m=audio") -> CallType.AUDIO
                else -> CallType.UNKNOWN
            }
        }

        fun sendNotification(notiContent : String,
                             sessionId : String,
                             currentUser : UserInstance,
                             receiver : UserInstance,
                             action : String) {
            val tokenList = ArrayList<String>()
            tokenList.add(receiver.token)
            sendMessageToServer(createCallMessage(
                notiContent,
                tokenList,
                sessionId,
                currentUser,
                receiver,
                action))
        }

        interface GetUserCallback{
            fun onSuccess(users : List<UserInstance>)
            fun onFailure()
        }

        interface GetNewCallback{
            fun onSuccess(news : List<NewsInstance>,
                          lastTimePosted : Double?,
                          lastKey : String)
            fun onFailure()
        }

        interface GetNotificationCallback{
            fun onSuccess(notifications : List<NotificationInstance>)
            fun onFailure()
        }

        interface SignInGoogleCallback{
            fun onSuccess(email : String)
            fun onFailure()
        }

        interface FetchSignInMethodCallback{
            fun onSuccess(result : Pair<Boolean, String>)
            fun onFailure(result : Pair<Boolean, String>)
        }

        interface SendPasswordResetEmailCallback{
            fun onSuccess()
            fun onFailure()
        }

        interface SaveSignUpInformationCallBack{
            fun onSuccess()
            fun onFailure()
        }

        interface BasicCallBack{
            fun onSuccess()
            fun onFailure()
        }

        interface CallStatusCallBack{
            fun onSuccess(status : CallStatus)
            fun onFailure()
        }
    }
}