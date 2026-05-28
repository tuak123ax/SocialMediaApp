package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.InviteFriendToGroupUseCase
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class InviteMemberViewModel(
    private val copyLinkUseCase : CopyLinkUseCase,
    private val getUserUseCase : GetUserUseCase,
    private val inviteFriendToGroupUseCase : InviteFriendToGroupUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun copyLink(copyData : String) {
        viewModelScope.launch(ioDispatcher) {
            copyLinkUseCase.invoke(copyData)
        }
    }

    suspend fun findUserById(userId: String) : UserInstance? {
        return getUserUseCase.invoke(userId, false)
    }

    fun inviteFriendToGroup(
        currentUser : UserInstance,
        friend: UserInstance,
        group : GroupInstance) {
        viewModelScope.launch(ioDispatcher) {
            //Create noti object
            val inviteToGroupNotiContent = "${currentUser.name} invited you to join group: ${group.name}"
            val notification = NotificationInstance(
                getRandomIdForNotification(),
                inviteToGroupNotiContent,
                currentUser.image,
                currentUser.uid,
                getCurrentTime(),
                NotificationType.INVITE_TO_GROUP,
                group.id)
            //Send Notification
            if(friend.token.isNotEmpty()){
                if(notification.content.isNotEmpty()) {
                    val friendToken = ArrayList<String>()
                    friendToken.add(friend.token)
                    sendMessageToServer(
                        createMessageForServer(
                            notification.content,
                            friendToken,
                        currentUser,
                            "BASIC"))
                }
            }

            //Save invite notification to friend notification list
            friend.notifications.add(notification)
            logMessage("friendNotification", { "size: " + friend.notifications.size })
            inviteFriendToGroupUseCase.invoke(
                friend)
        }
    }
}