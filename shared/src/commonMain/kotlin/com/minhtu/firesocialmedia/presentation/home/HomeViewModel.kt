package com.minhtu.firesocialmedia.presentation.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationType
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetNewCallback
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetNotificationCallback
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    val listState = LazyListState()
    var listNews: ArrayList<NewsInstance> = ArrayList()
    var listNotificationOfCurrentUser = mutableStateListOf<NotificationInstance>()
    //Cache loaded users, only fetch new user if that user is not in this cache
    var loadedUsersCache : MutableSet<UserInstance?> = mutableSetOf()
    var currentUser: UserInstance? = null
    var currentUserState by mutableStateOf(currentUser)
    fun updateCurrentUser(user: UserInstance, platform: PlatformContext) {
        currentUser = user
        currentUserState = currentUser
        updateFCMTokenForCurrentUser(user, platform)
        likeCache = currentUser!!.likedPosts
    }

    val _getCurrentUserStatus = mutableStateOf(false)
    val getCurrentUserStatus = _getCurrentUserStatus
    fun getCurrentUserAndFriends(platform: PlatformContext) {
        viewModelScope.launch(ioDispatcher) {
            try{
                val currentUserId = platform.auth.getCurrentUserUid()
                if(currentUserId != null) {
                    val user = platform.database.getUser(
                        currentUserId)
                    if(user != null) {
                        updateCurrentUser(user, platform)
                        _getCurrentUserStatus.value = true
                        getAllUserFriends(user, platform)
                    } else {
                        logMessage("getCurrentUser",
                            { "Current user is null" })
                        _getCurrentUserStatus.value = false
                    }
                } else {
                    logMessage("getCurrentUser",
                        { "Current user id is null" })
                    _getCurrentUserStatus.value = false
                }
            } catch (ex : Exception) {
                logMessage("getCurrentUser",
                    { "Cannot get current user: " + ex.message.toString() })
                _getCurrentUserStatus.value = false
            }
        }
    }

     fun getAllUserFriends(user: UserInstance, platform: PlatformContext) {
         viewModelScope.launch(ioDispatcher) {
             val friendIds = user.friends
             // Thresholds
             val maxParallel = 20
             val chunkSize = 10
             val friends = if (friendIds.size <= maxParallel) {
                 // Fetch all in parallel
                 friendIds.map { friendId ->
                     async {platform.database.getUser(friendId)}
                 }.awaitAll()
             } else {
                 // Batch mode
                 val resultList = mutableListOf<UserInstance>()
                 val batches = friendIds.chunked(chunkSize)
                 for (batch in batches) {
                     val batchResults = batch.map { id ->
                         async { platform.database.getUser(id) }
                     }.awaitAll().filterNotNull()
                     resultList.addAll(batchResults)
                 }
                 resultList
             }

             updateUserFriends(ArrayList(friends))
         }
    }

    val _getAllNewsStatus = mutableStateOf(false)
    val getAllNewsStatus = _getAllNewsStatus
    var isLoadingMore = mutableStateOf(false)
    var hasMoreData = mutableStateOf(true)
    private var lastTimePosted: Double? = null
    private var lastKey: String? = null
    fun getLatestNews(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if (!isLoadingMore.value && hasMoreData.value) {
                    isLoadingMore.value = true
                    platform.database.getLatestNews(
                        10,
                        lastTimePosted,
                        lastKey,
                        Constants.NEWS_PATH,
                        object : GetNewCallback {
                            override fun onSuccess(
                                news: List<NewsInstance>,
                                lastTimePostedValue: Double?,
                                lastKeyValue : String
                            ) {
                                updateNews(ArrayList(news))
                                for (new in news) {
                                    listNews.add(new)
                                    addLikeCountData(new.id, new.likeCount)
                                    addCommentCountData(new.id, new.commentCount)
                                }
                                _getAllNewsStatus.value = true
                                isLoadingMore.value = false
                                if(lastTimePostedValue == null) {
                                    hasMoreData.value = false
                                }
                                lastTimePosted = lastTimePostedValue
                                lastKey = lastKeyValue
                                checkUsersInCacheAndGetMore(platform)
                            }

                            override fun onFailure() {
                                _getAllNewsStatus.value = false
                                isLoadingMore.value = false
                            }
                        })
                }
            }
        }
    }

    fun checkUsersInCacheAndGetMore(
        platform: PlatformContext
    ) {
        viewModelScope.launch(ioDispatcher) {
            val allPosterIds = listNews.map { it.posterId }.distinct()
            val missingPosterIds = allPosterIds.filterNot { posterId ->
                loadedUsersCache.any( {it?.uid == posterId} )
            }

            if(missingPosterIds.isNotEmpty()) {
                try{
                    val newUsers = missingPosterIds.map { posterId ->
                        async {findUserById(posterId, platform)}
                    }.awaitAll().filterNotNull()
                    // Add to the cache + global set
                    loadedUsersCache.addAll(newUsers)
                } catch (e : Exception) {
                    logMessage("checkUsersInCacheAndGetMore",
                        { "Exception when get more users: " + e.message.toString() })
                }
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

    private val _allUserFriends = MutableStateFlow<List<UserInstance?>>(emptyList())
    val allUserFriends = _allUserFriends.asStateFlow()
    fun updateUserFriends(users: ArrayList<UserInstance?>) {
        _allUserFriends.value = users
        //Add loaded user friends to cache
        loadedUsersCache = (loadedUsersCache + users).toMutableSet()
    }

    private var _allNews = MutableStateFlow<MutableSet<NewsInstance>>(mutableSetOf())
    val allNews = _allNews.asStateFlow()
    fun updateNews(news: ArrayList<NewsInstance>) {
        _allNews.value = (_allNews.value + news).toMutableSet()
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
                        saveAndSendNotification(currentUser!!, new, platform)
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
        val poster = platform.database.getUser(selectedNew.posterId)
        if(poster != null) {
            Utils.saveNotification(notification, poster, platform)
            //Send notification to poster
            val tokenList = ArrayList<String>()
            tokenList.add(poster.token)
            sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser, "BASIC"))
        }
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

    //----------------------------CALL FEATURE-----------------------------------//

    var isInCall = MutableStateFlow<Boolean>(false)

    fun updateIsInCall(input : Boolean) {
        isInCall.value = input
    }
    fun observePhoneCall(
        platform: PlatformContext,
        onNavigateToCallingScreen: suspend (String, String, String, OfferAnswer) -> Unit,
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
                                viewModelScope.launch(ioDispatcher) {
                                    onNavigateToCallingScreen(sessionId, callerId, calleeId, offer)
                                }
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
    //---------------------------------------------------------------------------//

    fun loadMoreNews(platform : PlatformContext) {
        if(isLoadingMore.value) return
        logMessage("loadMoreNews", { "start load more new" })
        viewModelScope.launch(ioDispatcher) {
            getLatestNews(platform)
        }
    }

    suspend fun findUserById(userId: String, platform: PlatformContext) : UserInstance? {
        return platform.database.getUser(userId)
    }

    suspend fun searchUserByName(name: String, platform: PlatformContext) : List<UserInstance>{
        if(name.isBlank()) return emptyList()
        val resultList = platform.database.searchUserByName(
            name,
            Constants.USER_PATH
        )
        if(resultList == null) return emptyList()
        else return resultList
    }
}