package com.minhtu.firesocialmedia.presentation.navigationscreen.notification

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class NotificationViewModel (
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(){
    var allNeededUsers : MutableSet<UserInstance?> = mutableSetOf()
    val _getNeededUsersStatus = MutableStateFlow(false)
    val getNeededUsersStatus = _getNeededUsersStatus
    fun checkUsersInCacheAndGetMore(
        loadedUsersCache: MutableSet<UserInstance?>,
        allNotifications: SnapshotStateList<NotificationInstance>,
        platform: PlatformContext
    ) {
        viewModelScope.launch(ioDispatcher) {
            //Add loaded user cache to current set.
            allNeededUsers = loadedUsersCache
            val allSenderIds = allNotifications.map { it.sender }.distinct()
            val missingSenderIds = allSenderIds.filterNot { senderId ->
                loadedUsersCache.any( {it?.uid == senderId} )
            }

            if(missingSenderIds.isNotEmpty()) {
                try{
                    val newUsers = missingSenderIds.map { senderId ->
                        async {findUserById(senderId, platform)}
                    }.awaitAll().filterNotNull()
                    // Add to the cache + global set
                    allNeededUsers.addAll(newUsers)
                    loadedUsersCache.addAll(newUsers)
                } catch (e : Exception) {
                    logMessage("checkUsersInCacheAndGetMore",
                        { "Exception when get more users: " + e.message.toString() })
                }
            }
            
            // Always set status to true when operation completes, regardless of whether new users were fetched
            _getNeededUsersStatus.value = true
        }
    }

    suspend fun findUserById(userId: String, platform: PlatformContext) : UserInstance? {
        return platform.database.getUser(userId)
    }

    fun findLoadedUserInSet(userId: String): UserInstance? {
        return allNeededUsers.firstOrNull { it?.uid == userId }
    }

    suspend fun requestFindNewById(newId: String, platform: PlatformContext) : NewsInstance? {
        return platform.database.getNew(newId)
    }

    fun onNotificationClick(
        notification: NotificationInstance,
        listNews : ArrayList<NewsInstance>,
        platform: PlatformContext,
        onNavigateToPostInformation: (NewsInstance) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch(ioDispatcher) {
            var relatedNew = Utils.findNewById(notification.relatedInfo, listNews)

            if (relatedNew == null) {
                relatedNew = requestFindNewById(notification.relatedInfo, platform)
            }
            withContext(Dispatchers.Main) {
                if (relatedNew != null) {
                    onNavigateToPostInformation(relatedNew)
                } else {
                    onError()
                }
            }
        }
    }
}