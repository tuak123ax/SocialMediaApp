package com.minhtu.firesocialmedia.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.home.CallInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
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
    private val userInteractor: UserInteractor,
    private val newsInteractor: NewsInteractor,
    private val notificationInteractor: NotificationInteractor,
    private val callInteractor: CallInteractor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var listNews: ArrayList<NewsInstance> = ArrayList()
    var listNotificationOfCurrentUser = mutableStateListOf<NotificationInstance>()
    //Cache loaded users, only fetch new user if that user is not in this cache
    var loadedUsersCache : MutableSet<UserInstance?> = mutableSetOf()
    var currentUser: UserInstance? = null
    var currentUserState by mutableStateOf(currentUser)
    suspend fun updateCurrentUser(user: UserInstance) {
        currentUser = user
        currentUserState = currentUser
        updateFCMTokenForCurrentUser(user)
        likeCache = currentUser!!.likedPosts
    }

    val _getCurrentUserStatus = mutableStateOf(false)
    val getCurrentUserStatus = _getCurrentUserStatus
    suspend fun getCurrentUserAndFriends() {
        try{
            val currentUserId = userInteractor.getCurrentUserId()
            if(currentUserId != null) {
                val user = userInteractor.getUser(currentUserId)
                if(user != null) {
                    updateCurrentUser(user)
                    _getCurrentUserStatus.value = true
                    getAllUserFriends(user)
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

    fun getAllUserFriends(user: UserInstance) {
        viewModelScope.launch(ioDispatcher) {
            val friendIds = user.friends
            // Thresholds
            val maxParallel = 20
            val chunkSize = 10
            val friends = if (friendIds.size <= maxParallel) {
                // Fetch all in parallel
                friendIds.map { friendId ->
                    async {
                        userInteractor.getUser(friendId)
                    }
                }.awaitAll()
            } else {
                // Batch mode
                val resultList = mutableListOf<UserInstance>()
                val batches = friendIds.chunked(chunkSize)
                for (batch in batches) {
                    val batchResults = batch.map { id ->
                        async {
                            userInteractor.getUser(id)
                        }
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
    suspend fun getLatestNews() {
        if (!isLoadingMore.value && hasMoreData.value) {
            isLoadingMore.value = true
            try{
                val latestNewsResult = newsInteractor.pageLatest(
                    10,
                    lastTimePosted,
                    lastKey
                )
                if(latestNewsResult != null) {
                    if(latestNewsResult.news != null) {
                        addNews(ArrayList(latestNewsResult.news))
                        for (new in latestNewsResult.news) {
                            listNews.add(new)
                            addLikeCountData(new.id, new.likeCount)
                            addCommentCountData(new.id, new.commentCount)
                        }
                        _getAllNewsStatus.value = true
                        if(latestNewsResult.lastTimePostedValue == null) {
                            hasMoreData.value = false
                        }
                        lastTimePosted = latestNewsResult.lastTimePostedValue
                        lastKey = latestNewsResult.lastKeyValue
                        checkUsersInCacheAndGetMore()
                    }
                } else {
                    _getAllNewsStatus.value = false
                }
            } finally { isLoadingMore.value = false }
        }
    }

    fun checkUsersInCacheAndGetMore() {
        viewModelScope.launch(ioDispatcher) {
            val allPosterIds = listNews.map { it.posterId }.distinct()
            val missingPosterIds = allPosterIds.filterNot { posterId ->
                loadedUsersCache.any( {it?.uid == posterId} )
            }

            if(missingPosterIds.isNotEmpty()) {
                try{
                    val newUsers = missingPosterIds.map { posterId ->
                        async {
                            userInteractor.getUser(posterId)
                        }
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
    fun getAllNotificationsOfUser() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val currentUserId = userInteractor.getCurrentUserId()
                if(currentUserId != null) {
                    val notifications = notificationInteractor.allNotificationsOf(
                        currentUserId)
                    if(notifications != null) {
                        listNotificationOfCurrentUser.clear()
                        listNotificationOfCurrentUser.addAll(notifications)
                        updateNotifications(ArrayList(listNotificationOfCurrentUser.toList()))
                        _getAllNotificationsOfCurrentUser.value = true
                    } else {
                        _getAllNotificationsOfCurrentUser.value = false
                    }
                }
            }
        }
    }

    fun removeNotificationInList(notification: NotificationInstance) {
        listNotificationOfCurrentUser.remove(notification)
    }

    private suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {
        userInteractor.updateFcmToken(user)
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
    fun addNews(news: ArrayList<NewsInstance>) {
        _allNews.value = (_allNews.value + news).toMutableSet()
    }
    fun updateNews(news: ArrayList<NewsInstance>) {
        _allNews.value = news.toMutableSet()
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

    fun clearAccountInStorage() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                userInteractor.clearLocalAccount()
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

    fun clickLikeButton(news: NewsInstance) {
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
            sendLikeUpdatesToFirebase(HashMap(_likeCountList.value))
        }
    }

    val saveLikeDataStatus = mutableStateOf(false)
    val updateCountAndSendNotiStatus = mutableStateOf(false)
    private suspend fun sendLikeUpdatesToFirebase(
        likeCountList: HashMap<String, Int>,
    ) {
        if(currentUser != null) {
            currentUser!!.likedPosts = likeCache
            val result = userInteractor.saveLikedPost(
                currentUser!!.uid,
                likeCache)
            saveLikeDataStatus.value = result
            try {
                for (likedNew in likeCache.keys) {
                    if (likeCountList[likedNew] != null) {
                        newsInteractor.like(
                            likedNew,
                            likeCountList[likedNew]!!
                        )
                        val new = Utils.findNewById(likedNew, listNews)
                        //Save and send notification
                        if (new != null) {
                            saveAndSendNotification(currentUser!!, new)
                        }
                    }
                }
                for (unlikedNew in unlikeCache) {
                    if (likeCountList[unlikedNew] != null) {
                        newsInteractor.unlike(
                            unlikedNew,
                            likeCountList[unlikedNew]!!
                        )
                    }
                }
                updateCountAndSendNotiStatus.value = true
            } catch (e: Exception) {
                logMessage("sendLikeUpdatesToFirebase", { e.message.toString() })
                updateCountAndSendNotiStatus.value = false
            }
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
        selectedNew: NewsInstance
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
        val poster = userInteractor.getUser(selectedNew.posterId)
        if(poster != null) {
            poster.addNotification(notification)
            notificationInteractor.saveNotificationToDatabase(
                poster.uid,
                poster.notifications
            )
            //Send notification to poster
            val tokenList = ArrayList<String>()
            tokenList.add(poster.token)
            sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser, "BASIC"))
        }
    }

    suspend fun deleteNotification(notification: NotificationInstance) {
        if(currentUser != null) {
            currentUser!!.notifications.remove(notification)
            notificationInteractor.deleteNotificationFromDatabase(
                currentUser!!.uid,
                notification
            )
        }
    }

    fun deleteOrHideNew(action: String, new: NewsInstance) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            if (action == "Delete") {
                newsInteractor.delete(
                    new
                )
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
    suspend fun observePhoneCall(
        onNavigateToCallingScreen: suspend (String, String, String, OfferAnswer) -> Unit,
        onNavigateBack: () -> Unit
    ) {
        if(currentUser != null) {
            try{
                callInteractor.observe(
                    isInCall,
                    currentUser!!.uid,
                    onReceivePhoneCallRequest = {remoteSessionId, remoteCallerId, remoteCalleeId, remoteOffer ->
                        if(remoteCalleeId == currentUser!!.uid) {
                            viewModelScope.launch(ioDispatcher) {
                                onNavigateToCallingScreen(remoteSessionId, remoteCallerId, remoteCalleeId, remoteOffer)
                            }
                        }
                    },
                    onEndCall = {
                        onNavigateBack()
                    }
                )
            } catch(e : Exception){
                logMessage("observePhoneCall Exception", { e.message.toString() })
            }
        }
    }
    //---------------------------------------------------------------------------//

    fun loadMoreNews() {
        if(isLoadingMore.value) return
        viewModelScope.launch(ioDispatcher) {
            getLatestNews()
        }
    }

    suspend fun findUserById(userId: String) : UserInstance? {
        return userInteractor.getUser(userId)
    }

    suspend fun searchUserByName(name: String) : List<UserInstance>{
        if(name.isBlank()) return emptyList()
        val resultList = userInteractor.searchUserByName(name)
        return resultList ?: emptyList()
    }
}