package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.home.CallInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // no-op
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test get current user and friends success`() = runTest(testDispatcher) {
        val currentUser = UserInstance(email = "email", image = "avatar", name = "me", uid = "currentUserUid", token = "token").apply {
            friends = arrayListOf("friend1", "friend2")
        }
        val friend1 = UserInstance(uid = "friend1", name = "friend1")
        val friend2 = UserInstance(uid = "friend2", name = "friend2")
        val fakeUserInteractor = object : UserInteractor {
            override suspend fun getCurrentUserId(): String? = "currentUserUid"
            override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? =
                when (userId) { "currentUserUid" -> currentUser; "friend1" -> friend1; "friend2" -> friend2; else -> null }
            override suspend fun updateFcmToken(user: UserInstance) {}
            override suspend fun saveCurrentUserInfo(user: UserInstance) {}
            override suspend fun clearLocalAccount() {}
            override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
            override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
            override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
            override suspend fun clearLocalData() {}
        }
        val fakeNewsInteractor = object : NewsInteractor {
            override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
            override suspend fun like(id: String, value: Int) {}
            override suspend fun unlike(id: String, value: Int) {}
            override suspend fun delete(new: NewsInstance) {}
            override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
        }
        val fakeNotificationInteractor = object : NotificationInteractor {
            override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = emptyList()
            override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
            override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
            override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
        }
        val fakeCallInteractor = object : CallInteractor {
            override suspend fun observe(isInCall: MutableStateFlow<Boolean>, userId: String, onReceivePhoneCallRequest: suspend (com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData) -> Unit, onEndCall: suspend () -> Unit, whoEndCallCallBack: suspend (String) -> Unit) {}
            override fun stopObservePhoneCall() {}
            override suspend fun stopCallService() {}
        }
        val homeViewModel = HomeViewModel(fakeUserInteractor, fakeNewsInteractor, fakeNotificationInteractor, fakeCallInteractor, testDispatcher)
        homeViewModel.getCurrentUserAndFriends()
        advanceUntilIdle()

        val getCurrentUserStatus = homeViewModel.getCurrentUserStatus.value
        assertEquals(true, getCurrentUserStatus)
    }

    @Test
    fun `test get current user and friends fail`() = runTest(testDispatcher) {
        val fakeUserInteractor = object : UserInteractor {
            override suspend fun getCurrentUserId(): String? = null
            override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
            override suspend fun updateFcmToken(user: UserInstance) {}
            override suspend fun saveCurrentUserInfo(user: UserInstance) {}
            override suspend fun clearLocalAccount() {}
            override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
            override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
            override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
            override suspend fun clearLocalData() {}
        }
        val homeViewModel = HomeViewModel(
            fakeUserInteractor,
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
            },
            object : NotificationInteractor {
                override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = emptyList()
                override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
                override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
                override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
            },
            object : CallInteractor {
                override suspend fun observe(isInCall: MutableStateFlow<Boolean>, userId: String, onReceivePhoneCallRequest: suspend (com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData) -> Unit, onEndCall: suspend () -> Unit, whoEndCallCallBack: suspend (String) -> Unit) {}
                override fun stopObservePhoneCall() {}
                override suspend fun stopCallService() {}
            },
            testDispatcher
        )
        homeViewModel.getCurrentUserAndFriends()
        advanceUntilIdle()

        val getCurrentUserStatus = homeViewModel.getCurrentUserStatus.value
        assertEquals(false, getCurrentUserStatus)
    }

    @Test
    fun `test get latest news success`() = runTest(testDispatcher) {
        val news = listOf(NewsInstance(id = "testNew1", posterId = "p1"), NewsInstance(id = "testNew2", posterId = "p2"))
        val homeViewModel = HomeViewModel(
            object : UserInteractor {
                override suspend fun getCurrentUserId(): String? = null
                override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
                override suspend fun updateFcmToken(user: UserInstance) {}
                override suspend fun saveCurrentUserInfo(user: UserInstance) {}
                override suspend fun clearLocalAccount() {}
                override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
                override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
                override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
                override suspend fun clearLocalData() {}
            },
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = LatestNewsResult(news, null, null)
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
            },
            object : NotificationInteractor {
                override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = emptyList()
                override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
                override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
                override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
            },
            object : CallInteractor {
                override suspend fun observe(isInCall: MutableStateFlow<Boolean>, userId: String, onReceivePhoneCallRequest: suspend (com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData) -> Unit, onEndCall: suspend () -> Unit, whoEndCallCallBack: suspend (String) -> Unit) {}
                override fun stopObservePhoneCall() {}
                override suspend fun stopCallService() {}
            },
            testDispatcher
        )
        homeViewModel.getLatestNews()
        advanceUntilIdle()

        val getAllNewsStatus = homeViewModel.getAllNewsStatus.value
        assertEquals(true, getAllNewsStatus)
    }

    @Test
    fun `test get latest news fail`() = runTest(testDispatcher) {
        val homeViewModel = HomeViewModel(
            object : UserInteractor {
                override suspend fun getCurrentUserId(): String? = null
                override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
                override suspend fun updateFcmToken(user: UserInstance) {}
                override suspend fun saveCurrentUserInfo(user: UserInstance) {}
                override suspend fun clearLocalAccount() {}
                override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
                override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
                override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
                override suspend fun clearLocalData() {}
            },
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
            },
            object : NotificationInteractor {
                override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = emptyList()
                override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
                override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
                override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
            },
            object : CallInteractor {
                override suspend fun observe(isInCall: MutableStateFlow<Boolean>, userId: String, onReceivePhoneCallRequest: suspend (com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData) -> Unit, onEndCall: suspend () -> Unit, whoEndCallCallBack: suspend (String) -> Unit) {}
                override fun stopObservePhoneCall() {}
                override suspend fun stopCallService() {}
            },
            testDispatcher
        )
        homeViewModel.getLatestNews()
        advanceUntilIdle()

        val getAllNewsStatus = homeViewModel.getAllNewsStatus.value
        assertEquals(false, getAllNewsStatus)
    }

    @Test
    fun `test get all notification success`() = runTest(testDispatcher) {
        val notifications = listOf(
            NotificationInstance(id = "n1", content = "", avatar = "", sender = "s1", timeSend = 0L, type = NotificationType.LIKE, relatedInfo = "r1"),
            NotificationInstance(id = "n2", content = "", avatar = "", sender = "s2", timeSend = 0L, type = NotificationType.LIKE, relatedInfo = "r2")
        )
        val fakeUserInteractor = object : UserInteractor {
            override suspend fun getCurrentUserId(): String? = "currentUser"
            override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
            override suspend fun updateFcmToken(user: UserInstance) {}
            override suspend fun saveCurrentUserInfo(user: UserInstance) {}
            override suspend fun clearLocalAccount() {}
            override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
            override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
            override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
            override suspend fun clearLocalData() {}
        }
        val homeViewModel = HomeViewModel(
            fakeUserInteractor,
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
            },
            object : NotificationInteractor {
                override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = notifications
                override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
                override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
                override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
            },
            object : CallInteractor {
                override suspend fun observe(isInCall: MutableStateFlow<Boolean>, userId: String, onReceivePhoneCallRequest: suspend (com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData) -> Unit, onEndCall: suspend () -> Unit, whoEndCallCallBack: suspend (String) -> Unit) {}
                override fun stopObservePhoneCall() {}
                override suspend fun stopCallService() {}
            },
            testDispatcher
        )
        homeViewModel.getAllNotificationsOfUser()
        advanceUntilIdle()

        val getAllNotifications = homeViewModel.getAllNotificationsOfCurrentUser.value
        assertEquals(true, getAllNotifications)
    }

    @Test
    fun `test get all notification fail`() = runTest(testDispatcher) {
        val fakeUserInteractor = object : UserInteractor {
            override suspend fun getCurrentUserId(): String? = "currentUser"
            override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
            override suspend fun updateFcmToken(user: UserInstance) {}
            override suspend fun saveCurrentUserInfo(user: UserInstance) {}
            override suspend fun clearLocalAccount() {}
            override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
            override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
            override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
            override suspend fun clearLocalData() {}
        }
        val homeViewModel = HomeViewModel(
            fakeUserInteractor,
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
            },
            object : NotificationInteractor {
                override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = null
                override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
                override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
                override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
            },
            object : CallInteractor {
                override suspend fun observe(isInCall: MutableStateFlow<Boolean>, userId: String, onReceivePhoneCallRequest: suspend (com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData) -> Unit, onEndCall: suspend () -> Unit, whoEndCallCallBack: suspend (String) -> Unit) {}
                override fun stopObservePhoneCall() {}
                override suspend fun stopCallService() {}
            },
            testDispatcher
        )
        homeViewModel.getAllNotificationsOfUser()
        advanceUntilIdle()

        val getAllNotifications = homeViewModel.getAllNotificationsOfCurrentUser.value
        assertEquals(false, getAllNotifications)
    }
}