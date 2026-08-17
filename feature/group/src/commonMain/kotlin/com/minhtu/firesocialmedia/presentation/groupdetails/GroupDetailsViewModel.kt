package com.minhtu.firesocialmedia.presentation.groupdetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.storage.group.SupabaseStorageProvider
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.UpdateNotificationStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.DeletePollUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailsViewModel(
    private val fetchGroupInfoUseCase: FetchGroupInfoUseCase,
    private val updateNotificationStatusUseCase: UpdateNotificationStatusUseCase,
    private val fetchNotificationStateUseCase: FetchNotificationStateUseCase,
    private val findGroupByIdUseCase: FindGroupByIdUseCase,
    private val joinGroupUseCase: JoinGroupUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val leaveAndDeleteGroupUseCase: LeaveAndDeleteGroupUseCase,
    private val deletePollUseCase: DeletePollUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var coverPhoto by mutableStateOf(SupabaseStorageProvider.DEFAULT_AVATAR_URL)
    fun updateCover(input: String) {
        coverPhoto = input
    }

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

    private val _notificationState = MutableStateFlow<Boolean?>(null)
    var notificationState = _notificationState.asStateFlow()
    private val _updateNotificationState = MutableStateFlow<Boolean?>(null)
    var updateNotificationState = _updateNotificationState.asStateFlow()
    fun updateNotificationStateWhenClickButton() {
        if (_notificationState.value != null) {
            _notificationState.value = !_notificationState.value!!
        }
    }
    fun updateNotificationStatus(
        groupId: String,
        userId: String
    ) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                updateNotificationStateWhenClickButton()
                if (_notificationState.value != null) {
                    _updateNotificationState.value = updateNotificationStatusUseCase.invoke(
                        _notificationState.value!!,
                        groupId,
                        userId
                    )
                }
            }
        }
    }

    fun fetchNotificationState(userId: String, groupId: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                _notificationState.value = fetchNotificationStateUseCase.invoke(
                    userId,
                    groupId
                )
            }
        }
    }

    private val _groupDetailsFromDeepLink = MutableStateFlow<GroupInstance?>(null)
    var groupDetailsFromDeepLink = _groupDetailsFromDeepLink.asStateFlow()
    suspend fun requestFindGroupDetailsById(groupId: String) {
        _groupDetailsFromDeepLink.value = findGroupByIdUseCase.invoke(groupId)
    }

    private val _joinGroupStatus = MutableStateFlow<Boolean?>(null)
    var joinGroupStatus = _joinGroupStatus.asStateFlow()
    fun joinGroup(currentUser: UserInstance, group: GroupInstance) {
        viewModelScope.launch(ioDispatcher) {
            _joinGroupStatus.value = joinGroupUseCase.invoke(currentUser, group)
        }
    }

    fun resetJoinGroupState() {
        _joinGroupStatus.value = null
    }

    private val _leaveGroupStatus = MutableStateFlow<Boolean?>(null)
    var leaveGroupStatus = _leaveGroupStatus.asStateFlow()
    fun leaveGroup(
        currentUser: UserInstance,
        fetchGroupInfoState: GroupInstance
    ) {
        viewModelScope.launch(ioDispatcher) {
            if (fetchGroupInfoState.members.size > 1) {
                // If the group has other members, only leave.
                _leaveGroupStatus.value = leaveGroupUseCase.invoke(
                    currentUser,
                    fetchGroupInfoState
                )
            } else {
                // If you are the only member of the group, delete the group.
                _leaveGroupStatus.value = leaveAndDeleteGroupUseCase.invoke(
                    currentUser,
                    fetchGroupInfoState
                )
            }
            fetchGroupInfoState.members.remove(currentUser.uid)
            currentUser.groups.remove(fetchGroupInfoState.id)
        }
    }
    fun resetLeaveGroupStatus() {
        _leaveGroupStatus.value = null
    }

    private val _deletePollState = MutableStateFlow<Boolean?>(null)
    val deletePollState = _deletePollState.asStateFlow()
    fun deletePoll(news: NewsInstance, groupId: String) {
        val pollId = news.pollId ?: return
        viewModelScope.launch(ioDispatcher) {
            _deletePollState.value = deletePollUseCase.invoke(news.id, pollId, groupId)
        }
    }
    fun resetDeletePollState() {
        _deletePollState.value = null
    }

    //--------------Limit photos before pass to Compose----------------//
    private val pageSize = 30
    private var currentPage = 0
    private var allPosts: List<NewsInstance> = emptyList()
    private var loading = false

    private val _visibleImages = mutableStateListOf<String>()
    val visibleImages: List<String> = _visibleImages

    /** Initial load OR hard refresh */
    fun init(posts: List<NewsInstance>) {
        allPosts = posts.sortedByDescending { it.timePosted }
        _visibleImages.clear()
        currentPage = 0
        loadMore()
    }

    /** Called when new posts arrive */
    fun onPostsUpdated(posts: List<NewsInstance>) {
        if (posts.size <= allPosts.size) {
            return
        }

        val sorted = posts.sortedByDescending { it.timePosted }
        val newPosts = sorted.take(posts.size - allPosts.size)

        val newImages = newPosts.map { it.image }

        // Insert new images at top (newest first)
        _visibleImages.addAll(0, newImages)

        allPosts = sorted
    }

    fun loadMore() {
        if (loading) return
        loading = true

        val nextImages = allPosts
            .drop(currentPage * pageSize)
            .take(pageSize)
            .map { it.image }

        _visibleImages.addAll(nextImages)
        currentPage++
        loading = false
    }
}

