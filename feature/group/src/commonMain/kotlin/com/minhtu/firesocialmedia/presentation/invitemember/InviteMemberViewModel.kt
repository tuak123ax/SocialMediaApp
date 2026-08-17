package com.minhtu.firesocialmedia.presentation.invitemember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.group.entity.notification.NotificationType
import com.minhtu.firesocialmedia.group.entity.notification.toSharedNotification
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.group.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.group.platform.sendMessageToServer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InviteMemberViewModel(
    private val copyLinkUseCase : CopyLinkUseCase,
    private val getUserUseCase : GetUserUseCase,
    private val saveNotificationToDatabaseUseCase : SaveNotificationToDatabaseUseCase,
    private val fetchGroupInfoUseCase : FetchGroupInfoUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _fetchGroupInfoState = MutableStateFlow<GroupInstance?>(null)
    var fetchGroupInfoState = _fetchGroupInfoState.asStateFlow()
    fun fetchGroupInfo(groupId: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                _fetchGroupInfoState.value = fetchGroupInfoUseCase.invoke(groupId)
            }
        }
    }
    fun resetFetchGroupInfoState() {
        _fetchGroupInfoState.value = null
    }

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
                id = getRandomIdForNotification(),
                content = inviteToGroupNotiContent,
                avatar = currentUser.image,
                sender = currentUser.uid,
                timeSend = getCurrentTime(),
                type = NotificationType.INVITE_TO_GROUP,
                relatedInfo = group.id
            )
            //Send Notification
            if(friend.token.isNotEmpty()){
                if(notification.content.isNotEmpty()) {
                    val friendToken = ArrayList<String>()
                    friendToken.add(friend.token)
                    sendMessageToServer(
                        createMessageForServer(
                            notification.content,
                            friendToken,
                            currentUser.token,
                            currentUser.uid,
                            currentUser.image,
                            currentUser.email,
                            currentUser.name,
                            "BASIC"))
                }
            }

            //Save invite notification to friend notification list
            friend.notifications.add(notification)
            logMessage("friendNotification", { "size: " + friend.notifications.size })
            saveNotificationToDatabaseUseCase.invoke(
                friend.uid,
                ArrayList(friend.notifications.map { it.toSharedNotification() })
            )
        }
    }
}
