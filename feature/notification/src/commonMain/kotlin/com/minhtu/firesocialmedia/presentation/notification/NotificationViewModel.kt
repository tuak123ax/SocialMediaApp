package com.minhtu.firesocialmedia.presentation.notification

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.minhtu.firesocialmedia.notification.entity.news.NewsInstance
import com.minhtu.firesocialmedia.notification.entity.news.isDefaultNewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.notification.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteAllNotificationsUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.UpdateIsReadStatusOfNotificationUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.presentation.notification.instance.BasicResult
import com.minhtu.firesocialmedia.notification.utils.Utils
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NotificationViewModel (
    private val getUserUseCase: GetUserUseCase,
    private val findNewByIdInDbUseCase : FindNewByIdInDbUseCase,
    private val updateIsReadStatusOfNotificationUseCase : UpdateIsReadStatusOfNotificationUseCase,
    private val deleteAllNotificationsUseCase : DeleteAllNotificationsUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(){
    var allNeededUsers : HashMap<String,UserInstance?> = HashMap()
    val _getNeededUsersStatus = MutableStateFlow(false)
    val getNeededUsersStatus = _getNeededUsersStatus
    private val cacheMutex = Mutex()
    fun checkUsersInCacheAndGetMore(
        loadedUsersCache: HashMap<String,UserInstance?>,
        allNotifications: SnapshotStateList<NotificationInstance>
    ) {
        viewModelScope.launch(ioDispatcher) {
            //Add loaded user cache to current set.
            allNeededUsers = loadedUsersCache
            val allSenderIds = allNotifications.map { it.sender }.distinct()
            val missingSenderIds = allSenderIds.filterNot { senderId ->
                loadedUsersCache.any( {it.key == senderId} )
            }

            if(missingSenderIds.isNotEmpty()) {
                try{
                    val newUsers: List<Pair<String, UserInstance?>> = supervisorScope {
                        missingSenderIds.map { senderId ->
                            async { senderId to runCatching { getUserUseCase.invoke(senderId, false) }.getOrNull() }
                        }.awaitAll()
                    }

                    cacheMutex.withLock {
                        for ((id, user) in newUsers) {
                            if (user != null) {
                                allNeededUsers[id] = user
                                loadedUsersCache[id] = user
                            }
                        }
                    }
                } catch (e : Exception) {
                    logMessage("checkUsersInCacheAndGetMore",
                        { "Exception when get more users: " + e.message.toString() })
                }
            }

            // Always set status to true when operation completes, regardless of whether new users were fetched
            _getNeededUsersStatus.value = true
        }
    }

    fun findLoadedUserInSet(userId: String): UserInstance? {
        return allNeededUsers[userId]
    }

    suspend fun requestFindNewById(newId: String) : NewsInstance? {
        return findNewByIdInDbUseCase.invoke(newId)
    }

    fun onNotificationClick(
        notification: NotificationInstance,
        listNews : ArrayList<NewsInstance>,
        onNavigateToPostInformation: (NewsInstance) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch(ioDispatcher) {
            var relatedNew = Utils.findNewById(notification.relatedInfo, listNews)

            if (relatedNew == null) {
                relatedNew = requestFindNewById(notification.relatedInfo)
            }
            withContext(Dispatchers.Main) {
                if (relatedNew != null && !relatedNew.isDefaultNewsInstance()) {
                    onNavigateToPostInformation(relatedNew)
                } else {
                    onError()
                }
            }
        }
    }

    suspend fun updateIsReadStatusOfNotification(
        updatedNotification: NotificationInstance,
        user: UserInstance
    ) {
        val updatedNotifications = user.notifications.map {
            if (it.id == updatedNotification.id) updatedNotification else it
        }

        user.notifications = ArrayList(updatedNotifications)

        updateIsReadStatusOfNotificationUseCase.invoke(
            user.uid,
            updatedNotification
        )
    }

    private val _deleteAllNotificationsStatus = MutableStateFlow<BasicResult?>(null)
    var deleteAllNotificationsStatus = _deleteAllNotificationsStatus.asStateFlow()
    fun deleteAllNotifications(userId : String) {
        viewModelScope.launch(ioDispatcher) {
            val result = deleteAllNotificationsUseCase.invoke(userId)
            if(result.isSuccess) {
                _deleteAllNotificationsStatus.value = BasicResult(true)
            } else {
                val error = result.exceptionOrNull()
                if(error != null && error.message != null && error.message!!.contains("network", ignoreCase = true)) {
                    _deleteAllNotificationsStatus.value = BasicResult(false, "Please recheck your network!")
                } else {
                    _deleteAllNotificationsStatus.value = BasicResult(false, "Cannot delete notifications. Please try again!")
                }
            }
        }
    }

    fun resetDeleteAllNotificationsStatus() {
        _deleteAllNotificationsStatus.value = null
    }
}