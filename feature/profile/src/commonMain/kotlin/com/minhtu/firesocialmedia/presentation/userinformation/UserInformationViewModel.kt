package com.minhtu.firesocialmedia.presentation.userinformation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.domain.usecases.network.CheckInternetConnectionUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.GetNewsByUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserBackgroundUseCase
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.profile.entity.notification.NotificationType
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.profile.platform.createMessageForServer
import com.minhtu.firesocialmedia.profile.platform.sendMessageToServer
import com.minhtu.firesocialmedia.profile.utils.Utils
import com.minhtu.firesocialmedia.storage.profile.SupabaseStorageProvider
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Relationship {
    FRIEND,
    FRIEND_REQUEST,
    WAITING_RESPONSE,
    NONE
}

class UserInformationViewModel(
    private val saveFriendUseCase: ProfileSaveFriendUseCase,
    private val saveFriendRequestUseCase: ProfileSaveFriendRequestUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val checkCalleeAvailableUseCase: CheckCalleeAvailableUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val updateUserBackgroundUseCase: UpdateUserBackgroundUseCase,
    private val getNewsByUserUseCase: GetNewsByUserUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    // Local URI of a newly picked cover photo (prior to upload)
    var coverPhoto by mutableStateOf(SupabaseStorageProvider.DEFAULT_AVATAR_URL)
    fun updateCover(input: String) {
        coverPhoto = input
    }

    // Local URI of successfully uploaded background (show without refetch)
    var uploadedBackgroundUri by mutableStateOf<String?>(null)
        private set

    private val _backgroundUploadStatus = MutableStateFlow<Boolean?>(null)
    val backgroundUploadStatus = _backgroundUploadStatus.asStateFlow()

    fun uploadBackground(userId: String) {
        val uri = coverPhoto.takeIf { it != SupabaseStorageProvider.DEFAULT_AVATAR_URL } ?: return
        viewModelScope.launch(ioDispatcher) {
            val result = updateUserBackgroundUseCase(userId, uri)
            _backgroundUploadStatus.value = result
            if (result) {
                uploadedBackgroundUri = uri
                coverPhoto = SupabaseStorageProvider.DEFAULT_AVATAR_URL
            }
        }
    }

    fun resetBackgroundUploadStatus() {
        _backgroundUploadStatus.value = null
    }

    private var _addFriendStatus = MutableStateFlow<Relationship?>(null)
    var addFriendStatus = _addFriendStatus.asStateFlow()
    private var friendRequestList: ArrayList<String> = ArrayList()
    var currentRelationship: Relationship = Relationship.NONE
    private var updateFriendRequestJob: Job? = null
    suspend fun checkInternetConnection(): Boolean {
        return checkInternetConnectionUseCase.invoke().first()
    }

    fun clickAddFriendButton(friend: UserInstance?, currentUser: UserInstance?) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if (friend != null && currentUser != null) {
                    val tokenList = ArrayList<String>()
                    tokenList.add(friend.token)
                    updateFriendRequestJob?.cancel()
                    //Use background scope instead of viewModelScope here to prevent job cancellation
                    // when navigating to other screen.
                    val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    updateFriendRequestJob = backgroundScope.launch {
                        try {
                            when (currentRelationship) {
                                Relationship.FRIEND -> {
                                    friend.removeFriend(currentUser.uid)
                                    currentUser.removeFriend(friend.uid)
                                    removeFriend(friend, currentUser)
                                    _addFriendStatus.value = Relationship.NONE
                                }

                                Relationship.FRIEND_REQUEST -> {
                                    removeFriendRequest(friend, currentUser)
                                    friend.removeFriendRequest(currentUser.uid)
                                    _addFriendStatus.value = Relationship.NONE
                                }

                                Relationship.NONE -> {
                                    //Save friend request to db
                                    saveFriendRequest(friend, currentUser)
                                    friend.addFriendRequest(currentUser.uid)

                                    val notiContent =
                                        "${currentUser.name} sent you a friend request!"
                                    val notification = NotificationInstance(
                                        getRandomIdForNotification(),
                                        notiContent,
                                        currentUser.image,
                                        currentUser.uid,
                                        getCurrentTime(),
                                        NotificationType.ADD_FRIEND,
                                        currentUser.uid
                                    )
                                    //Save notification to db
                                    Utils.saveNotification(
                                        notification,
                                        friend,
                                        saveNotificationToDatabaseUseCase
                                    )
                                    sendMessageToServer(
                                        createMessageForServer(
                                            notiContent,
                                            tokenList,
                                            currentUser.token,
                                            currentUser.uid,
                                            currentUser.image,
                                            currentUser.email,
                                            currentUser.name,
                                            "BASIC"
                                        )
                                    )
                                    _addFriendStatus.value = Relationship.FRIEND_REQUEST
                                }

                                else -> {

                                }
                            }
                        } catch (e: Exception) {
                            logMessage("updateFriendRequestJob", { "Exception: ${e.message}" })
                        }
                    }
                }
            }
        }
    }

    fun checkRelationship(friend: UserInstance, currentUser: UserInstance): Relationship {
        return if (currentUser.friends.contains(friend.uid)) {
            Relationship.FRIEND
        } else if (currentUser.friendRequests.contains(friend.uid)) {
            Relationship.WAITING_RESPONSE
        } else {
            if (friend.friendRequests.contains(currentUser.uid)) {
                Relationship.FRIEND_REQUEST
            } else {
                Relationship.NONE
            }
        }
    }

    fun updateRelationship(relationship: Relationship) {
        this.currentRelationship = relationship
        _addFriendStatus.value = relationship
    }

    private suspend fun saveFriendRequest(friend: UserInstance, currentUser: UserInstance) {
        try {
            friendRequestList.add(currentUser.uid)

            saveFriendRequestUseCase.invoke(
                friend.uid,
                friendRequestList
            )
            _addFriendStatus.value = Relationship.FRIEND_REQUEST
        } catch (_: Exception) {
        }
    }

    private suspend fun removeFriendRequest(friend: UserInstance, currentUser: UserInstance) {
        try {
            friendRequestList.remove(currentUser.uid)
            saveFriendRequestUseCase.invoke(
                friend.uid,
                friendRequestList
            )
            _addFriendStatus.value = Relationship.NONE
        } catch (_: Exception) {
        }
    }

    private suspend fun removeFriend(friend: UserInstance, currentUser: UserInstance) {
        try {
            saveFriendUseCase.invoke(
                currentUser.uid,
                currentUser.friends
            )
        } catch (_: Exception) {
        }
        try {
            saveFriendUseCase.invoke(
                friend.uid,
                friend.friends
            )
        } catch (_: Exception) {
        }
        _addFriendStatus.value = Relationship.NONE
    }

    private val _calleeCurrentState = MutableStateFlow<Boolean?>(null)
    var calleeCurrentState = _calleeCurrentState.asStateFlow()
    fun checkCalleeAvailable(callee: UserInstance) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val result = checkCalleeAvailableUseCase.invoke(
                    callee.uid
                )
                _calleeCurrentState.value = result
            }
        }
    }

    fun resetCalleeState() {
        _calleeCurrentState.value = null
    }

    private val _fetchedUser = MutableStateFlow<UserInstance?>(null)
    var fetchedUser = _fetchedUser.asStateFlow()

    // News authored by the profile being viewed. Fetched and paginated by this screen directly —
    // Home's feed is no longer reused, since Home itself now only loads 10 posts at a time (not
    // the full feed), so it can no longer be trusted to already contain this user's posts.
    private val _userNews = MutableStateFlow<List<NewsInstance>>(emptyList())
    val userNews = _userNews.asStateFlow()

    private var lastTimePosted: Double? = null
    private var lastKey: String? = null
    var isLoadingMoreUserNews by mutableStateOf(false)
        private set
    var hasMoreUserNews by mutableStateOf(true)
        private set

    fun fetchUserInformation(userId: String, isCurrentUser: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            _fetchedUser.value = try {
                getUserUseCase.invoke(userId, isCurrentUser)
            } catch (_: Exception) {
                UserInstance()
            }
        }
    }

    // Resets pagination and fetches the first page of this user's posts. Call whenever the
    // viewed user changes (fresh navigation into the screen).
    fun fetchInitialUserNews(userId: String) {
        _userNews.value = emptyList()
        lastTimePosted = null
        lastKey = null
        hasMoreUserNews = true
        loadMoreUserNews(userId)
    }

    // Fetches (and keeps fetching, page by page) until we have PAGE_SIZE more posts by this user
    // or the backend feed runs out — see ProfileDatabaseService.getNewsByPoster. Call again
    // (e.g. on scroll-near-bottom) to fetch the next page.
    fun loadMoreUserNews(userId: String) {
        if (isLoadingMoreUserNews || !hasMoreUserNews) return
        viewModelScope.launch(ioDispatcher) {
            isLoadingMoreUserNews = true
            try {
                logMessage("loadMoreUserNews", { "invoked for $userId" })
                val page = getNewsByUserUseCase.invoke(userId, PAGE_SIZE, lastTimePosted, lastKey)
                val merged = LinkedHashMap<String, NewsInstance>()
                _userNews.value.forEach { news -> merged[news.id] = news }
                page.news.forEach { news -> merged[news.id] = news }
                _userNews.value = merged.values.toList()
                lastTimePosted = page.lastTimePosted
                lastKey = page.lastKey
                if (page.lastTimePosted == null) {
                    hasMoreUserNews = false
                }
                logMessage("loadMoreUserNews", { "Size: " + _userNews.value.size })
            } catch (_: Exception) {
                logMessage("loadMoreUserNews", { "Exception" })
            } finally {
                isLoadingMoreUserNews = false
            }
        }
    }

    fun resetOldData() {
        _fetchedUser.value = null
        coverPhoto = SupabaseStorageProvider.DEFAULT_AVATAR_URL
        uploadedBackgroundUri = null
        _addFriendStatus.value = null
        _userNews.value = emptyList()
        lastTimePosted = null
        lastKey = null
        isLoadingMoreUserNews = false
        hasMoreUserNews = true
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}