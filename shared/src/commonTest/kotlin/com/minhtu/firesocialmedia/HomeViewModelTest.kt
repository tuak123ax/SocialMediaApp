package com.minhtu.firesocialmedia

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.dto.news.NewsInstance
import com.minhtu.firesocialmedia.data.dto.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.dto.user.UserInstance
import com.minhtu.firesocialmedia.di.AuthServiceMock
import com.minhtu.firesocialmedia.di.DatabaseServiceMock
import com.minhtu.firesocialmedia.di.PlatformContextMock
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetNewCallback
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetNotificationCallback
import com.minhtu.firesocialmedia.utils.Utils.Companion.GetUserCallback
import io.mockative.coEvery
import io.mockative.eq
import io.mockative.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var platformContext: PlatformContextMock
    private lateinit var authServiceMock: AuthServiceMock
    private lateinit var databaseServiceMock: DatabaseServiceMock

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        platformContext = PlatformContextMock()
        authServiceMock = AuthServiceMock()
        databaseServiceMock = DatabaseServiceMock()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test get all users success`() = runTest(testDispatcher) {
        val homeViewModel = HomeViewModel(testDispatcher)

        every { platformContext.auth } returns authServiceMock
        every { platformContext.database } returns databaseServiceMock

        val currentUser = UserInstance(uid = "currentUserUid")
        coEvery { databaseServiceMock.updateFCMTokenForCurrentUser(currentUser) } returns Unit
        every { authServiceMock.getCurrentUserUid() } returns currentUser.uid
        every { databaseServiceMock.getAllUsers(eq(Constants.USER_PATH), any()) }
            .invokes { args ->
                val callback = args[1] as GetUserCallback
                val testList = ArrayList<UserInstance>()
                testList.add(currentUser)
                testList.add(UserInstance(uid = "testUser1"))
                testList.add(UserInstance(uid = "testUser2"))
                testList.add(UserInstance(uid = "testUser3"))
                testList.add(UserInstance(uid = "testUser4"))
                testList.add(UserInstance(uid = "testUser5"))
                callback.onSuccess(testList)
            }

        homeViewModel.getAllUsers(platformContext)
        advanceUntilIdle()

        val getAllUsersStatus = homeViewModel.getAllUsersStatus.value
        assertEquals(true, getAllUsersStatus)
    }

    @Test
    fun `test get all users fail`() = runTest(testDispatcher) {
        val homeViewModel = HomeViewModel(testDispatcher)

        every { platformContext.auth } returns authServiceMock
        every { platformContext.database } returns databaseServiceMock

        val currentUser = UserInstance(uid = "currentUserUid")
        coEvery { databaseServiceMock.updateFCMTokenForCurrentUser(currentUser) } returns Unit
        every { authServiceMock.getCurrentUserUid() } returns currentUser.uid
        every { databaseServiceMock.getAllUsers(eq(Constants.USER_PATH), any()) }
            .invokes { args ->
                val callback = args[1] as GetUserCallback
                callback.onFailure()
            }

        homeViewModel.getAllUsers(platformContext)
        advanceUntilIdle()

        val getAllUsersStatus = homeViewModel.getAllUsersStatus.value
        assertEquals(false, getAllUsersStatus)
    }

    @Test
    fun `test get all news success`() = runTest(testDispatcher) {
        val homeViewModel = HomeViewModel(testDispatcher)

        every { platformContext.auth } returns authServiceMock
        every { platformContext.database } returns databaseServiceMock

        every { databaseServiceMock.getAllNews(eq(Constants.NEWS_PATH), any()) }
            .invokes { args ->
                val callback = args[1] as GetNewCallback
                val testList = ArrayList<NewsInstance>()
                testList.add(NewsInstance(id = "testNew1"))
                testList.add(NewsInstance(id = "testNew2"))
                testList.add(NewsInstance(id = "testNew3"))
                testList.add(NewsInstance(id = "testNew4"))
                testList.add(NewsInstance(id = "testNew5"))
                callback.onSuccess(testList)
            }

        homeViewModel.getAllNews(platformContext)
        advanceUntilIdle()

        val getAllNewsStatus = homeViewModel.getAllNewsStatus.value
        assertEquals(true, getAllNewsStatus)
    }

    @Test
    fun `test get all news fail`() = runTest(testDispatcher) {
        val homeViewModel = HomeViewModel(testDispatcher)

        every { platformContext.auth } returns authServiceMock
        every { platformContext.database } returns databaseServiceMock

        every { databaseServiceMock.getAllNews(eq(Constants.NEWS_PATH), any()) }
            .invokes { args ->
                val callback = args[1] as GetNewCallback
                callback.onFailure()
            }

        homeViewModel.getAllNews(platformContext)
        advanceUntilIdle()

        val getAllNewsStatus = homeViewModel.getAllNewsStatus.value
        assertEquals(false, getAllNewsStatus)
    }

    @Test
    fun `test get all notification success`() = runTest(testDispatcher) {
        val homeViewModel = HomeViewModel(testDispatcher)

        every { platformContext.auth } returns authServiceMock
        every { platformContext.database } returns databaseServiceMock

        every { authServiceMock.getCurrentUserUid() } returns "currentUserUid"
        every { databaseServiceMock.getAllNotificationsOfUser(eq(Constants.NOTIFICATION_PATH),eq("currentUserUid"), any()) }
            .invokes { args ->
                val callback = args[2] as GetNotificationCallback
                val testList = ArrayList<NotificationInstance>()
                testList.add(NotificationInstance(id = "testNoti1"))
                testList.add(NotificationInstance(id = "testNoti2"))
                callback.onSuccess(testList)
            }

        homeViewModel.getAllNotificationsOfUser(platformContext)
        advanceUntilIdle()

        val getAllNotifications = homeViewModel.getAllNotificationsOfCurrentUser.value
        assertEquals(true, getAllNotifications)
    }

    @Test
    fun `test get all notification fail`() = runTest(testDispatcher) {
        val homeViewModel = HomeViewModel(testDispatcher)

        every { platformContext.auth } returns authServiceMock
        every { platformContext.database } returns databaseServiceMock

        every { authServiceMock.getCurrentUserUid() } returns "currentUserUid"
        every { databaseServiceMock.getAllNotificationsOfUser(eq(Constants.NOTIFICATION_PATH),eq("currentUserUid"), any()) }
            .invokes { args ->
                val callback = args[2] as GetNotificationCallback
                callback.onFailure()
            }

        homeViewModel.getAllNotificationsOfUser(platformContext)
        advanceUntilIdle()

        val getAllNotifications = homeViewModel.getAllNotificationsOfCurrentUser.value
        assertEquals(false, getAllNotifications)
    }
}