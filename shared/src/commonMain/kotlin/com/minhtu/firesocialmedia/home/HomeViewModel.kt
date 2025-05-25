package com.minhtu.firesocialmedia.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.createMessageForServer
import com.minhtu.firesocialmedia.getCurrentTime
import com.minhtu.firesocialmedia.getRandomIdForNotification
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.logMessage
import com.minhtu.firesocialmedia.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    val listState = LazyListState()
    var listUsers: ArrayList<UserInstance> = ArrayList()
    var listNews: ArrayList<NewsInstance> = ArrayList()
    var listNotificationOfCurrentUser = mutableStateListOf<NotificationInstance>()
    var currentUser : UserInstance? = null
    var currentUserState by mutableStateOf(currentUser)
    fun updateCurrentUser(user: UserInstance, platform: PlatformContext) {
        logMessage("updateCurrentUser", "likedPosts: "+ user.likedPosts.size)
        currentUser = user
        currentUserState = currentUser
        updateFCMTokenForCurrentUser(user, platform)
    }

    fun removeNotificationInList(notification: NotificationInstance) {
        listNotificationOfCurrentUser.remove(notification)
    }
    private fun updateFCMTokenForCurrentUser(user: UserInstance, platform: PlatformContext) {
        CoroutineScope(Dispatchers.IO).launch {
            platform.database.updateFCMTokenForCurrentUser(user)
        }
    }

    private val _allUsers = MutableStateFlow<List<UserInstance>>(emptyList())
    val allUsers = _allUsers.asStateFlow()
    fun updateUsers(users: ArrayList<UserInstance>) {
        _allUsers.value = users
        likeCache = currentUser!!.likedPosts
    }

    private var _allNews  = MutableStateFlow<ArrayList<NewsInstance>>(ArrayList())
    val allNews = _allNews.asStateFlow()
    fun updateNews(news: ArrayList<NewsInstance>) {
        _allNews.value.clear()
        _allNews.value = news
    }

    private val _allNotifications = MutableStateFlow<List<NotificationInstance>>(emptyList())
    val allNotifications = _allNotifications.asStateFlow()
    fun updateNotifications(notifications: ArrayList<NotificationInstance>) {
        _allNotifications.value = notifications
    }

    var numberOfListNeedToLoad by mutableIntStateOf(2)
    fun decreaseNumberOfListNeedToLoad(input : Int) {
        if(numberOfListNeedToLoad > 0) {
            numberOfListNeedToLoad -=  input
        }
    }

    fun clearAccountInStorage(platform: PlatformContext){
        platform.crypto.clearAccount()
    }

    //-----------------------------Like and comment function-----------------------------//
    // StateFlow to update UI in Compose
    private var _likedPosts = MutableStateFlow<HashMap<String,Boolean>>(HashMap())
    val likedPosts = _likedPosts.asStateFlow()
    private var likeCache : HashMap<String,Boolean> = HashMap()
    private var unlikeCache : ArrayList<String> = ArrayList()
    private var updateLikeJob : Job? = null
    private var _likeCountList = MutableStateFlow<HashMap<String,Int>>(HashMap())
    var likeCountList = _likeCountList
    fun addLikeCountData(newsId : String, likeCount : Int) {
        _likeCountList.value[newsId] = likeCount
    }
    fun clickLikeButton(news : NewsInstance, platform: PlatformContext) {
        val isLiked = likeCache[news.id] == true
        if (isLiked) {
            likeCache.remove(news.id) // Unlike
            unlikeCache.add(news.id)
            if(_likeCountList.value[news.id] != null) {
                _likeCountList.value[news.id] = _likeCountList.value[news.id]!! - 1
            }
        } else {
            likeCache[news.id] = true // Like
            unlikeCache.remove(news.id)
            if(_likeCountList.value[news.id] != null) {
                _likeCountList.value[news.id] = _likeCountList.value[news.id]!! + 1
            } else {
                _likeCountList.value[news.id] = 1
            }
        }

        _likedPosts.value = HashMap(likeCache)
        val tempLikeCountList = HashMap(_likeCountList.value)

        updateLikeJob?.cancel()
        //Use background scope instead of viewModelScope here to prevent job cancellation
        // when navigating to other screen.
        val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        updateLikeJob = backgroundScope.launch {
            sendLikeUpdatesToFirebase(tempLikeCountList, platform)
        }
    }

    private suspend fun sendLikeUpdatesToFirebase(likeCountList : HashMap<String,Int>, platform: PlatformContext) {
        currentUser!!.likedPosts = likeCache
        platform.database.saveValueToDatabase(currentUser!!.uid,
            Constants.USER_PATH, likeCache, Constants.LIKED_POSTS_PATH)
        for(likedNew in likeCache.keys) {
            if(likeCountList[likedNew] != null) {
                platform.database.updateCountValueInDatabase(likedNew,
                    Constants.NEWS_PATH,
                    Constants.LIKED_COUNT_PATH, likeCountList[likedNew]!!)
                try{
                    val new = Utils.findNewById(likedNew, listNews)
                    //Save and send notification
                    if(new != null) {
                        saveAndSendNotification(currentUser!!, new, listUsers, platform)
                    }
                } catch(e: Exception) {
                }
            }
        }
        for(unlikedNew in unlikeCache) {
            if(likeCountList[unlikedNew] != null) {
                platform.database.updateCountValueInDatabase(unlikedNew,
                    Constants.NEWS_PATH,
                    Constants.LIKED_COUNT_PATH, likeCountList[unlikedNew]!!)
            }
        }
    }

    fun updateLikeStatus(){
        _likedPosts.value = HashMap(likeCache)
    }

    //-----------------------------Comment-----------------------------//
    private var _commentCountList = MutableStateFlow<HashMap<String,Int>>(HashMap())
    var commentCountList = _commentCountList
    fun addCommentCountData(newsId : String, commentCount : Int) {
        _commentCountList.value[newsId] = commentCount
    }
    private var _commentStatus: MutableStateFlow<NewsInstance?> = MutableStateFlow(null)
    var commentStatus: StateFlow<NewsInstance?> = _commentStatus.asStateFlow()
    fun clickCommentButton(newsInstance: NewsInstance) {
        _commentStatus.value = newsInstance
    }
    fun resetCommentStatus() {
        _commentStatus.value = null
    }

    private suspend fun saveAndSendNotification(currentUser : UserInstance,
                                        selectedNew : NewsInstance,
                                        listUsers : ArrayList<UserInstance>,
                                        platform : PlatformContext) {
        val notiContent = "${currentUser.name} liked your post!"
        val notification = NotificationInstance(getRandomIdForNotification(),
            notiContent,
            currentUser.image,
            currentUser.uid,
            getCurrentTime(),
            NotificationType.LIKE,
            selectedNew.id)
        //Save notification to db
        val poster = Utils.findUserById(selectedNew.posterId, listUsers)
        Utils.saveNotification(notification, poster!!, platform)
        //Send notification to poster
        val tokenList = ArrayList<String>()
        tokenList.add(poster.token)
        sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser))
    }

    suspend fun deleteNotification(notification: NotificationInstance, platform : PlatformContext) {
        currentUser!!.notifications.remove(notification)
        Utils.deleteNotification(notification, currentUser!!, platform)
    }

    fun deleteOrHideNew(action : String, new: NewsInstance, platform: PlatformContext) {
        val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        backgroundScope.launch {
            if(action == "Delete") {
                platform.database.deleteNewsFromDatabase(Constants.NEWS_PATH, new)
            }
            listNews.remove(new)
            withContext(Dispatchers.Main) {
                updateNews(listNews)
            }
        }
    }
}