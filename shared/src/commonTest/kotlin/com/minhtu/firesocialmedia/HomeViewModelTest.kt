package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.constants.Constants
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() { /* no-op */ }

    @Test
    fun `test get current user and friends success`() = runTest {
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
            override suspend fun clearLocalFriends() {}
        }
        val fakeNewsInteractor = object : NewsInteractor {
            override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
            override suspend fun like(id: String, value: Int) {}
            override suspend fun unlike(id: String, value: Int) {}
            override suspend fun delete(new: NewsInstance) {}
            override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
            override suspend fun saveNews(news: NewsInstance): Boolean = true
            override suspend fun findNewById(newsId: String): NewsInstance? = null
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
        val homeViewModel = HomeViewModel(fakeUserInteractor, fakeNewsInteractor, fakeNotificationInteractor, fakeCallInteractor, StandardTestDispatcher(testScheduler))
        homeViewModel.getCurrentUserAndFriends()
        advanceUntilIdle()

        val getCurrentUserStatus = homeViewModel.getCurrentUserStatus.value
        assertEquals(true, getCurrentUserStatus)
    }

    @Test
    fun `test get current user and friends fail`() = runTest {
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
            override suspend fun clearLocalFriends() {}
        }
        val homeViewModel = HomeViewModel(
            fakeUserInteractor,
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
                override suspend fun saveNews(news: NewsInstance): Boolean = true
                override suspend fun findNewById(newsId: String): NewsInstance? = null
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
            StandardTestDispatcher(testScheduler)
        )
        homeViewModel.getCurrentUserAndFriends()
        advanceUntilIdle()

        val getCurrentUserStatus = homeViewModel.getCurrentUserStatus.value
        assertEquals(false, getCurrentUserStatus)
    }

    @Test
    fun `test get latest news success`() = runTest {
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
                override suspend fun clearLocalFriends() {}
            },
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = LatestNewsResult(news, null, null)
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
                override suspend fun saveNews(news: NewsInstance): Boolean = true
                override suspend fun findNewById(newsId: String): NewsInstance? = null
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
            StandardTestDispatcher(testScheduler)
        )
        homeViewModel.getLatestNews()
        advanceUntilIdle()

        val getAllNewsStatus = homeViewModel.getAllNewsStatus.value
        assertEquals(true, getAllNewsStatus)
    }

    @Test
    fun `test get latest news fail`() = runTest {
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
                override suspend fun clearLocalFriends() {}
            },
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
                override suspend fun saveNews(news: NewsInstance): Boolean = true
                override suspend fun findNewById(newsId: String): NewsInstance? = null
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
            StandardTestDispatcher(testScheduler)
        )
        homeViewModel.getLatestNews()
        advanceUntilIdle()

        val getAllNewsStatus = homeViewModel.getAllNewsStatus.value
        assertEquals(false, getAllNewsStatus)
    }

    @Test
    fun `test get all notification success`() = runTest {
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
            override suspend fun clearLocalFriends() {}
        }
        val homeViewModel = HomeViewModel(
            fakeUserInteractor,
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
                override suspend fun saveNews(news: NewsInstance): Boolean = true
                override suspend fun findNewById(newsId: String): NewsInstance? = null
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
            StandardTestDispatcher(testScheduler)
        )
        homeViewModel.getAllNotificationsOfUser()
        advanceUntilIdle()

        val getAllNotifications = homeViewModel.getAllNotificationsOfCurrentUser.value
        assertEquals(true, getAllNotifications)
    }

    @Test
    fun `test get all notification fail`() = runTest {
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
            override suspend fun clearLocalFriends() {}
        }
        val homeViewModel = HomeViewModel(
            fakeUserInteractor,
            object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
                override suspend fun saveNews(news: NewsInstance): Boolean = true
                override suspend fun findNewById(newsId: String): NewsInstance? = null
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
            StandardTestDispatcher(testScheduler)
        )
        homeViewModel.getAllNotificationsOfUser()
        advanceUntilIdle()

        val getAllNotifications = homeViewModel.getAllNotificationsOfCurrentUser.value
        assertEquals(false, getAllNotifications)
    }

    // ---------------------- Helper ----------------------
    private fun makeVm(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        userInteractor: UserInteractor = object : UserInteractor {
            override suspend fun getCurrentUserId(): String? = null
            override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
            override suspend fun updateFcmToken(user: UserInstance) {}
            override suspend fun saveCurrentUserInfo(user: UserInstance) {}
            override suspend fun clearLocalAccount() {}
            override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
            override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
            override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
            override suspend fun clearLocalData() {}
            override suspend fun clearLocalFriends() {}
        },
        newsInteractor: NewsInteractor = object : NewsInteractor {
            override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
            override suspend fun like(id: String, value: Int) {}
            override suspend fun unlike(id: String, value: Int) {}
            override suspend fun delete(new: NewsInstance) {}
            override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
            override suspend fun saveNews(news: NewsInstance): Boolean = true
            override suspend fun findNewById(newsId: String): NewsInstance? = null
        },
        notificationInteractor: NotificationInteractor = object : NotificationInteractor {
            override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = emptyList()
            override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
            override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
            override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
        }
    ): HomeViewModel = HomeViewModel(
        userInteractor,
        newsInteractor,
        notificationInteractor,
        object : CallInteractor {
            override suspend fun observe(isInCall: MutableStateFlow<Boolean>, userId: String, onReceivePhoneCallRequest: suspend (com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData) -> Unit, onEndCall: suspend () -> Unit, whoEndCallCallBack: suspend (String) -> Unit) {}
            override fun stopObservePhoneCall() {}
            override suspend fun stopCallService() {}
        },
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `searchUserByName with blank name returns empty list`() = runTest {
        val vm = makeVm(testScheduler)
        val result = vm.searchUserByName("")
        assertEquals(0, result.size)
    }

    @Test
    fun `searchUserByName with name delegates to interactor`() = runTest {
        val userList = listOf(UserInstance(uid = "u1", name = "Alice"), UserInstance(uid = "u2", name = "Alice2"))
        val vm = makeVm(
            testScheduler,
            userInteractor = object : UserInteractor {
                override suspend fun getCurrentUserId(): String? = null
                override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
                override suspend fun updateFcmToken(user: UserInstance) {}
                override suspend fun saveCurrentUserInfo(user: UserInstance) {}
                override suspend fun clearLocalAccount() {}
                override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
                override suspend fun searchUserByName(name: String) = if (name == "Alice") userList else emptyList()
                override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
                override suspend fun clearLocalData() {}
                override suspend fun clearLocalFriends() {}
            }
        )
        val result = vm.searchUserByName("Alice")
        assertEquals(2, result.size)
        assertEquals("u1", result[0].uid)
    }

    @Test
    fun `clickLikeButton likes and unlikes a post updating counts`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeVm(testScheduler)
        val user = UserInstance(uid = "u1", name = "Me")
        vm.currentUser = user
        vm.addLikeCountData("n1", 5)
        val news = NewsInstance(id = "n1")

        // First click: Like
        vm.clickLikeButton(news)
        advanceUntilIdle()
        assertEquals(1, vm.likedPosts.value["n1"])
        assertEquals(6, vm.likeCountList.value["n1"])

        // Second click: Unlike
        vm.clickLikeButton(news)
        advanceUntilIdle()
        assertEquals(null, vm.likedPosts.value["n1"])
        assertEquals(5, vm.likeCountList.value["n1"])
    }

    @Test
    fun `sharePost with no content sets shareError`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val vm = makeVm(testScheduler)
        vm.currentUser = UserInstance(uid = "u1")
        // No updateShareContent call
        vm.sharePost(UserInstance(uid = "u1"))
        advanceUntilIdle()
        assertEquals(Constants.POST_NEWS_EMPTY_ERROR, vm.shareError.value)
        Dispatchers.resetMain()
    }

    @Test
    fun `sharePost with content and no friends sets sharePostStatus true`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val vm = makeVm(
            testScheduler,
            newsInteractor = object : NewsInteractor {
                override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?) = null
                override suspend fun like(id: String, value: Int) {}
                override suspend fun unlike(id: String, value: Int) {}
                override suspend fun delete(new: NewsInstance) {}
                override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
                override suspend fun saveNews(news: NewsInstance): Boolean = true
                override suspend fun findNewById(newsId: String): NewsInstance? = null
            }
        )
        val user = UserInstance(uid = "u1", name = "Me", image = "img")
        user.friends = arrayListOf()  // no friends -> no notification loop
        vm.currentUser = user
        vm.updateShareContent(NewsInstance(id = "orig1"))
        vm.updateShareMessage("check this out")

        vm.sharePost(user)
        advanceUntilIdle()

        assertEquals(true, vm.sharePostStatus.value)
        Dispatchers.resetMain()
    }

    @Test
    fun `resetGetLatestNewsParams resets news loading state`() = runTest {
        val vm = makeVm(testScheduler)
        vm.isLoadingMore.value = true
        vm.hasMoreData.value = false
        vm.addLikeCountData("n1", 1)

        vm.resetGetLatestNewsParams()

        assertEquals(false, vm.getAllNewsStatus.value)
        assertEquals(false, vm.isLoadingMore.value)
        assertEquals(true, vm.hasMoreData.value)
    }

    @Test
    fun `isFriendOf returns true when user is friend`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateUserFriends(arrayListOf(UserInstance(uid = "friend1"), UserInstance(uid = "friend2")))
        assertEquals(true, vm.isFriendOf("friend1"))
        assertEquals(false, vm.isFriendOf("stranger"))
    }

    @Test
    fun `updateUserFriends updates state flow and cache`() = runTest {
        val vm = makeVm(testScheduler)
        val friends = arrayListOf<UserInstance?>(UserInstance(uid = "f1", name = "F1"), UserInstance(uid = "f2", name = "F2"))
        vm.updateUserFriends(friends)
        assertEquals(2, vm.allUserFriends.value.size)
        // cache should contain both friends
        assertEquals("F1", vm.findUserByIdInCache("f1")?.name)
    }

    @Test
    fun `findUserByIdInCache returns null for unknown user`() = runTest {
        val vm = makeVm(testScheduler)
        assertEquals(null, vm.findUserByIdInCache("nonexistent"))
    }

    @Test
    fun `removeNotificationInList removes the notification`() = runTest {
        val vm = makeVm(testScheduler)
        val notif = NotificationInstance(id = "n1", content = "msg", avatar = "", sender = "s", timeSend = 0L, type = NotificationType.LIKE, relatedInfo = "")
        vm.listNotificationOfCurrentUser.add(notif)
        assertEquals(1, vm.listNotificationOfCurrentUser.size)

        vm.removeNotificationInList(notif)
        assertEquals(0, vm.listNotificationOfCurrentUser.size)
    }

    @Test
    fun `resetShareContentAndStatus resets all share fields`() = runTest {
        val vm = makeVm(testScheduler)
        vm.currentUser = UserInstance(uid = "u1")
        vm.updateShareMessage("hello")
        vm.updateShareContent(NewsInstance(id = "x"))
        vm.sharePost(UserInstance(uid = "u1"))
        advanceUntilIdle()

        vm.resetShareContentAndStatus()

        assertEquals(null, vm.sharePostStatus.value)
        assertEquals(null, vm.shareError.value)
    }

    @Test
    fun `addLikeCountData and addCommentCountData populate maps`() = runTest {
        val vm = makeVm(testScheduler)
        vm.addLikeCountData("post1", 10)
        vm.addCommentCountData("post1", 3)
        assertEquals(10, vm.likeCountList.value["post1"])
        assertEquals(3, vm.commentCountList.value["post1"])
    }

    @Test
    fun `getLatestNews does not reload when isLoadingMore is true`() = runTest {
        val vm = makeVm(testScheduler)
        vm.isLoadingMore.value = true  // simulate already loading
        vm.getLatestNews()
        advanceUntilIdle()
        // Status stays default false since loading was blocked
        assertEquals(false, vm.getAllNewsStatus.value)
    }

    @Test
    fun `getLatestNews does not reload when hasMoreData is false`() = runTest {
        val vm = makeVm(testScheduler)
        vm.hasMoreData.value = false
        vm.getLatestNews()
        advanceUntilIdle()
        assertEquals(false, vm.getAllNewsStatus.value)
    }

    @Test
    fun `getCurrentUserAndFriends sets false when user data returns null`() = runTest {
        val vm = makeVm(
            testScheduler,
            userInteractor = object : UserInteractor {
                override suspend fun getCurrentUserId(): String? = "uid1"
                override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
                override suspend fun updateFcmToken(user: UserInstance) {}
                override suspend fun saveCurrentUserInfo(user: UserInstance) {}
                override suspend fun clearLocalAccount() {}
                override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
                override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
                override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
                override suspend fun clearLocalData() {}
                override suspend fun clearLocalFriends() {}
            }
        )
        vm.getCurrentUserAndFriends()
        advanceUntilIdle()
        assertEquals(false, vm.getCurrentUserStatus.value)
    }
}