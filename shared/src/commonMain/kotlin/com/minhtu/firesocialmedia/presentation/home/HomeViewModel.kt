package com.minhtu.firesocialmedia.presentation.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationType
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetNewCallback
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetNotificationCallback
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetUserCallback
import com.minhtu.firesocialmedia.utils.Utils.Companion.findUserById
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
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

class HomeViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    val listState = LazyListState()
    var listUsers: ArrayList<UserInstance> = ArrayList()
    var listNews: ArrayList<NewsInstance> = ArrayList()
    var listNotificationOfCurrentUser = mutableStateListOf<NotificationInstance>()
    var currentUser: UserInstance? = null
    var currentUserState by mutableStateOf(currentUser)
    fun updateCurrentUser(user: UserInstance, platform: PlatformContext) {
        currentUser = user
        currentUserState = currentUser
        updateFCMTokenForCurrentUser(user, platform)
    }

    val _getAllUsersStatus = mutableStateOf(false)
    val getAllUsersStatus = _getAllUsersStatus
    fun getAllUsers(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val currentUserId = platform.auth.getCurrentUserUid()
                platform.database.getAllUsers(Constants.USER_PATH, object : GetUserCallback {
                    override fun onSuccess(users: List<UserInstance>) {
                        val currentUser = findUserById(currentUserId!!, users)
                        updateCurrentUser(currentUser!!, platform)
                        val listAllUsers = ArrayList(users)
                        listAllUsers.remove(currentUser)
                        updateUsers(listAllUsers)
                        listUsers.clear()
                        listUsers.addAll(listAllUsers)
                        _getAllUsersStatus.value = true
                    }

                    override fun onFailure() {
                        _getAllUsersStatus.value = false
                    }
                })
            }
        }
    }

    val _getAllNewsStatus = mutableStateOf(false)
    val getAllNewsStatus = _getAllNewsStatus
    fun getAllNews(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.database.getAllNews(Constants.NEWS_PATH, object : GetNewCallback {
                    override fun onSuccess(news: List<NewsInstance>) {
                        updateNews(ArrayList(news))
                        listNews.clear()
                        for (new in news) {
                            listNews.add(new)
                            addLikeCountData(new.id, new.likeCount)
                            addCommentCountData(new.id, new.commentCount)
                        }
                        _getAllNewsStatus.value = true
                    }

                    override fun onFailure() {
                        _getAllNewsStatus.value = false
                    }

                })
            }
        }
    }

    val _getAllNotificationsOfCurrentUser = mutableStateOf(false)
    val getAllNotificationsOfCurrentUser = _getAllNotificationsOfCurrentUser
    fun getAllNotificationsOfUser(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val currentUserId = platform.auth.getCurrentUserUid()
                platform.database.getAllNotificationsOfUser(
                    Constants.NOTIFICATION_PATH,
                    currentUserId!!,
                    object : GetNotificationCallback {
                        override fun onSuccess(notifications: List<NotificationInstance>) {
                            listNotificationOfCurrentUser.clear()
                            listNotificationOfCurrentUser.addAll(notifications)
                            updateNotifications(ArrayList(listNotificationOfCurrentUser.toList()))
                            _getAllNotificationsOfCurrentUser.value = true
                        }

                        override fun onFailure() {
                            _getAllNotificationsOfCurrentUser.value = false
                        }

                    })
            }
        }
    }

    fun removeNotificationInList(notification: NotificationInstance) {
        listNotificationOfCurrentUser.remove(notification)
    }

    private fun updateFCMTokenForCurrentUser(user: UserInstance, platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.database.updateFCMTokenForCurrentUser(user)
            }
        }
    }

    private val _allUsers = MutableStateFlow<List<UserInstance>>(emptyList())
    val allUsers = _allUsers.asStateFlow()
    fun updateUsers(users: ArrayList<UserInstance>) {
        _allUsers.value = users
        likeCache = currentUser!!.likedPosts
    }

    private var _allNews = MutableStateFlow<ArrayList<NewsInstance>>(ArrayList())
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
    fun decreaseNumberOfListNeedToLoad(input: Int) {
        if (numberOfListNeedToLoad > 0) {
            numberOfListNeedToLoad -= input
        }
    }

    fun clearAccountInStorage(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                platform.crypto.clearAccount()
            }
        }
    }

    //-----------------------------Like and comment function-----------------------------//
    // StateFlow to update UI in Compose
    private var _likedPosts = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val likedPosts = _likedPosts.asStateFlow()
    private var likeCache: HashMap<String, Int> = HashMap()
    private var unlikeCache: ArrayList<String> = ArrayList()
    private var updateLikeJob: Job? = null
    private var _likeCountList = MutableStateFlow<HashMap<String, Int>>(HashMap())
    var likeCountList = _likeCountList
    fun addLikeCountData(newsId: String, likeCount: Int) {
        _likeCountList.value[newsId] = likeCount
    }

    fun clickLikeButton(news: NewsInstance, platform: PlatformContext) {
        val isLiked = likeCache[news.id] == 1
        if (isLiked) {
            likeCache.remove(news.id) // Unlike
            unlikeCache.add(news.id)
            if (_likeCountList.value[news.id] != null) {
                _likeCountList.value[news.id] = _likeCountList.value[news.id]!! - 1
            }
        } else {
            likeCache[news.id] = 1 // Like
            unlikeCache.remove(news.id)
            if (_likeCountList.value[news.id] != null) {
                _likeCountList.value[news.id] = _likeCountList.value[news.id]!! + 1
            } else {
                _likeCountList.value[news.id] = 1
            }
        }

        _likedPosts.value = HashMap(likeCache)

        updateLikeJob?.cancel()
        //Use background scope instead of viewModelScope here to prevent job cancellation
        // when navigating to other screen.
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        updateLikeJob = backgroundScope.launch {
            sendLikeUpdatesToFirebase(HashMap(_likeCountList.value), platform)
        }
    }

    val saveLikeDataStatus = mutableStateOf(false)
    val updateCountAndSendNotiStatus = mutableStateOf(false)
    private suspend fun sendLikeUpdatesToFirebase(
        likeCountList: HashMap<String, Int>,
        platform: PlatformContext
    ) {
        currentUser!!.likedPosts = likeCache
        platform.database.saveValueToDatabase(
            currentUser!!.uid,
            Constants.USER_PATH,
            likeCache,
            Constants.LIKED_POSTS_PATH,
            object : Utils.Companion.BasicCallBack {
                override fun onSuccess() {
                    saveLikeDataStatus.value = true
                }

                override fun onFailure() {
                    saveLikeDataStatus.value = false
                }
            })
        try {
            for (likedNew in likeCache.keys) {
                if (likeCountList[likedNew] != null) {
                    platform.database.updateCountValueInDatabase(
                        likedNew,
                        Constants.NEWS_PATH,
                        Constants.LIKED_COUNT_PATH, likeCountList[likedNew]!!
                    )
                    val new = Utils.findNewById(likedNew, listNews)
                    //Save and send notification
                    if (new != null) {
                        saveAndSendNotification(currentUser!!, new, listUsers, platform)
                    }
                }
            }
            for (unlikedNew in unlikeCache) {
                if (likeCountList[unlikedNew] != null) {
                    platform.database.updateCountValueInDatabase(
                        unlikedNew,
                        Constants.NEWS_PATH,
                        Constants.LIKED_COUNT_PATH, likeCountList[unlikedNew]!!
                    )
                }
            }
            updateCountAndSendNotiStatus.value = true
        } catch (e: Exception) {
            logMessage("sendLikeUpdatesToFirebase", { e.message.toString() })
            updateCountAndSendNotiStatus.value = false
        }
    }

    fun updateLikeStatus() {
        _likedPosts.value = HashMap(likeCache)
    }

    //-----------------------------Comment-----------------------------//
    private var _commentCountList = MutableStateFlow<HashMap<String, Int>>(HashMap())
    var commentCountList = _commentCountList
    fun addCommentCountData(newsId: String, commentCount: Int) {
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

    private suspend fun saveAndSendNotification(
        currentUser: UserInstance,
        selectedNew: NewsInstance,
        listUsers: ArrayList<UserInstance>,
        platform: PlatformContext
    ) {
        val notiContent = "${currentUser.name} liked your post!"
        val notification = NotificationInstance(
            getRandomIdForNotification(),
            notiContent,
            currentUser.image,
            currentUser.uid,
            getCurrentTime(),
            NotificationType.LIKE,
            selectedNew.id
        )
        //Save notification to db
        val poster = findUserById(selectedNew.posterId, listUsers)
        Utils.saveNotification(notification, poster!!, platform)
        //Send notification to poster
        val tokenList = ArrayList<String>()
        tokenList.add(poster.token)
        sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser, "BASIC"))
    }

    suspend fun deleteNotification(notification: NotificationInstance, platform: PlatformContext) {
        currentUser!!.notifications.remove(notification)
        Utils.deleteNotification(notification, currentUser!!, platform)
    }

    fun deleteOrHideNew(action: String, new: NewsInstance, platform: PlatformContext) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            if (action == "Delete") {
                platform.database.deleteNewsFromDatabase(Constants.NEWS_PATH, new)
            }
            listNews.remove(new)
            withContext(Dispatchers.Main) {
                updateNews(listNews)
            }
        }
    }

    var isInCall = MutableStateFlow<Boolean>(false)

    fun updateIsInCall(input : Boolean) {
        isInCall.value = input
    }
    fun observePhoneCall(
        platform: PlatformContext,
        onNavigateToCallingScreen: (String, String, String, OfferAnswer) -> Unit,
        onNavigateBack: () -> Unit
    ) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try{
                    platform.database.observePhoneCall(
                        isInCall,
                        currentUser!!.uid,
                        Constants.CALL_PATH,
                        phoneCallCallBack = { sessionId,callerId, calleeId, offer ->
                            if(calleeId == currentUser!!.uid) {
                                onNavigateToCallingScreen(sessionId, callerId, calleeId, offer)
                            }
                        },
                        endCallSession = { end ->
                            if(end) {
                                onNavigateBack()
                            }
                        },
                        iceCandidateCallBack = { iceCandidates ->
                            if(iceCandidates != null) {
                                for(candidate in iceCandidates.values) {
                                    if(candidate.candidate != null && candidate.sdpMid != null && candidate.sdpMLineIndex != null) {
                                        viewModelScope.launch(ioDispatcher) {
                                            platform.audioCall.addIceCandidate(candidate.candidate!!, candidate.sdpMid!!, candidate.sdpMLineIndex!!)
                                        }
                                    }
                                }
                            }
                        })
                } catch(e : Exception){
                    logMessage("observePhoneCall Exception", { e.message.toString() })
                }
            }
        }
    }
}