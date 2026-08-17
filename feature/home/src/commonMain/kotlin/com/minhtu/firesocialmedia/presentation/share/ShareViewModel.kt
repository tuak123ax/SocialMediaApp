package com.minhtu.firesocialmedia.presentation.share

import com.minhtu.firesocialmedia.constants.home.Constants
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.home.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.home.entity.notification.NotificationType
import com.minhtu.firesocialmedia.home.entity.notification.toSharedNotification
import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.home.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.generateRandomId
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.home.platform.sendMessageToServer
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ShareViewModel(
    private val userInteractor: UserInteractor,
    private val newsInteractor: NewsInteractor,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _shareMessage = MutableStateFlow("")
    private val _shareContent = MutableStateFlow<NewsInstance?>(null)
    private val _sharePostStatus = MutableStateFlow<Boolean?>(null)
    val sharePostStatus = _sharePostStatus.asStateFlow()
    private val _shareError = MutableStateFlow<String?>(null)
    val shareError = _shareError.asStateFlow()

    fun updateShareMessage(message: String) {
        _shareMessage.value = message
    }

    fun updateShareContent(news: NewsInstance) {
        logMessage("updateShareContent", { "id: " + news.id + " message:" + news.message })
        _shareContent.value = news
    }

    fun sharePost(currentUser: UserInstance?) {
        val user = currentUser ?: return
        viewModelScope.launch(ioDispatcher) {
            val newsRandomId = generateRandomId()
            if (_shareContent.value != null) {
                logMessage("sharePost", { "id: ${_shareContent.value!!.id}" })
                //Save post to db
                val newsInstance = NewsInstance(
                    newsRandomId,
                    user.uid,
                    user.name,
                    user.image,
                    _shareMessage.value,
                    shareContentId = _shareContent.value!!.id
                )
                newsInstance.timePosted = getCurrentTime()
                _sharePostStatus.value = newsInteractor.saveNews(
                    newsInstance
                )

                //Create noti object
                val notiContent = _shareMessage.value
                val notification = NotificationInstance(
                    getRandomIdForNotification(),
                    notiContent, user.image,
                    user.uid,
                    getCurrentTime(),
                    NotificationType.SHARE_NEW,
                    newsInstance.id
                )
                //Send Notification
                val friendTokens = getFriendTokens(user.friends)
                if (friendTokens.isNotEmpty()) {
                    if (notification.content.isNotEmpty()) {
                        sendMessageToServer(createMessageForServer(notification.content, friendTokens, user.token, user.uid, user.image, user.email, user.name, "BASIC"))
                    } else {
                        val content = "Shared a post!"
                        notification.updateContent(content)
                        sendMessageToServer(createMessageForServer(content, friendTokens, user.token, user.uid, user.image, user.email, user.name, "BASIC"))
                    }
                }

                //Save notification to db
                for (friend in user.friends) {
                    saveNotification(notification, friend)
                }
            } else {
                _shareError.value = Constants.POST_NEWS_EMPTY_ERROR
            }
        }
    }

    private suspend fun getFriendTokens(friendIds: List<String>): ArrayList<String> {
        val friendTokens = ArrayList<String>()
        for (friendId in friendIds) {
            val user = withContext(ioDispatcher) { userInteractor.getUser(friendId, false) }
            if (user != null) {
                friendTokens.add(user.token)
            }
        }
        return friendTokens
    }

    private suspend fun saveNotification(
        notification: NotificationInstance,
        friendId: String
    ) {
        //Save notification to friend's notification list
        try {
            val friend = userInteractor.getUser(friendId, false) ?: return
            friend.addNotification(notification)
            saveNotificationToDatabaseUseCase.invoke(
                friend.uid,
                ArrayList(friend.notifications.map { it.toSharedNotification() })
            )
        } catch (_: Exception) {
        }
    }

    fun resetShareContentAndStatus() {
        _shareMessage.value = ""
        _shareContent.value = null
        _sharePostStatus.value = null
        _shareError.value = null
    }
}
