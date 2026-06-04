package com.minhtu.firesocialmedia.presentation.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.minhtu.firesocialmedia.core.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract exposing the public API of HomeViewModel.
 *
 * Lives in :shared so that shared consumers (Navigation, Friend, Settings, Notification,
 * GroupDetails, UserInformation, PostInformation, Search, UiUtils, Calling, UploadNewfeed)
 * can reference the home view model without depending on :feature:home.
 *
 * The concrete implementation [HomeViewModel] lives in :feature:home.
 */
interface HomeViewModelContract {
    // Current user
    var currentUser: UserInstance?
    var currentUserState: UserInstance?
    val getCurrentUserStatus: MutableState<Boolean>
    fun getCurrentUserAndFriends()
    suspend fun updateCurrentUser(user: UserInstance)
    suspend fun findUserById(userId: String): UserInstance?
    fun findUserByIdInCache(userId: String): UserInstance?
    suspend fun searchUserByName(name: String): List<UserInstance>
    fun ensureUserLoaded(userId: String)

    // User cache
    var loadedUsersCache: HashMap<String, UserInstance?>
    val loadedUserState: StateFlow<Map<String, UserInstance?>>

    // Friends
    val allUserFriends: StateFlow<List<UserInstance?>>
    fun updateUserFriends(users: ArrayList<UserInstance?>)
    suspend fun getAllUserFriends(user: UserInstance)
    fun isFriendOf(posterId: String): Boolean

    // News
    var listNews: ArrayList<NewsInstance>
    val allNews: StateFlow<List<NewsInstance>>
    val getAllNewsStatus: MutableState<Boolean>
    var isLoadingMore: MutableState<Boolean>
    var hasMoreData: MutableState<Boolean>
    var isRefreshing: MutableState<Boolean>
    fun getLatestNews()
    fun loadMoreNews()
    fun refreshNews()
    fun resetGetLatestNewsParams()
    fun addNews(news: ArrayList<NewsInstance>)
    fun updateNews(news: ArrayList<NewsInstance>)
    fun deleteOrHideNew(action: String, new: NewsInstance)
    fun deletePoll(news: NewsInstance, groupId: String)
    var numberOfListNeedToLoad: Int
    fun decreaseNumberOfListNeedToLoad(input: Int)
    fun checkUsersInCacheAndGetMore()

    // Shared news
    val sharedNewsById: StateFlow<Map<String, NewsInstance?>>
    suspend fun ensureSharedNew(sharedNewId: String)

    // Notifications
    var listNotificationOfCurrentUser: SnapshotStateList<NotificationInstance>
    val allNotifications: StateFlow<List<NotificationInstance>>
    val getAllNotificationsOfCurrentUser: MutableState<Boolean>
    fun getAllNotificationsOfUser()
    fun removeNotificationInList(notification: NotificationInstance)
    fun updateNotifications(notifications: ArrayList<NotificationInstance>)
    suspend fun deleteNotification(notification: NotificationInstance)

    // Like & comment
    val likedPosts: StateFlow<HashMap<String, Int>>
    var likeCountList: MutableStateFlow<HashMap<String, Int>>
    var commentCountList: MutableStateFlow<HashMap<String, Int>>
    fun clickLikeButton(news: NewsInstance)
    fun updateLikeStatus()
    fun addLikeCountData(newsId: String, likeCount: Int)
    fun addCommentCountData(newsId: String, commentCount: Int)
    val commentStatus: StateFlow<NewsInstance?>
    fun clickCommentButton(newsInstance: NewsInstance)
    fun resetCommentStatus()

    // Share
    val sharePostStatus: StateFlow<Boolean?>
    val shareError: StateFlow<String?>
    fun updateShareMessage(message: String)
    fun updateShareContent(news: NewsInstance)
    fun sharePost(user: UserInstance)
    fun resetShareContentAndStatus()
    suspend fun getFriendTokens(currentUser: UserInstance): ArrayList<String>
    suspend fun saveNotification(notification: NotificationInstance, friend: UserInstance)

    // Call
    val isInCall: MutableStateFlow<Boolean>
    val endCallStatus: StateFlow<Boolean>
    val phoneCallRequestStatus: StateFlow<CallingRequestData?>
    fun updateIsInCall(input: Boolean)
    fun setWhoStopCall(input: String)
    fun resetCallEvent()
    fun observePhoneCall()
    fun stopObservePhoneCall()
    fun resetPhoneCallRequestStatus()
    fun resetEndCallStatus()

    // Account
    fun clearAccountInStorage()
    fun clearLocalData()
}
