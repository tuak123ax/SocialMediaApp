package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eygraber.uri.Uri
import com.minhtu.firesocialmedia.core.constants.AuthRouteNames
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.home.entity.call.CallingRequestData as HomeCallingRequestData
import com.minhtu.firesocialmedia.home.entity.call.OfferAnswer as HomeOfferAnswer
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance as HomeNewsInstance
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.entity.user.toSearchUser
import com.minhtu.firesocialmedia.home.entity.user.toDto as toHomeUserDto
import com.minhtu.firesocialmedia.entity.bridge.toAppInitUserDto
import com.minhtu.firesocialmedia.entity.bridge.toCommentUserDto
import com.minhtu.firesocialmedia.comment.entity.user.toCommentUser
import com.minhtu.firesocialmedia.comment.entity.user.toDto as toCommentUserDto
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository
import com.minhtu.firesocialmedia.domain.usecases.sync.SyncDataUseCase
import com.minhtu.firesocialmedia.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.setupSignInLauncher
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.navigation.CommentNavGraphImpl
import com.minhtu.firesocialmedia.presentation.comment.CommentFeatureViewModel
import com.minhtu.firesocialmedia.presentation.audiocall.CallViewModel
import com.minhtu.firesocialmedia.presentation.notification.NotificationsViewModel
import com.minhtu.firesocialmedia.presentation.settings.AccountViewModel
import com.minhtu.firesocialmedia.presentation.share.ShareViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.loading.GifLoading
import com.minhtu.firesocialmedia.presentation.loading.SyncLoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.Screen
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformation
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformationViewModel
import com.minhtu.firesocialmedia.presentation.showimage.ShowImage
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel
import com.minhtu.firesocialmedia.search.utils.UiUtils
import com.seiko.imageloader.LocalImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// Bridges feature/calling's CallingRequestData into feature/home's own local, decoupled
// CallingRequestData shape (home has no Gradle dependency on calling).
private fun CallingRequestData.toHomeCallingRequestData(): HomeCallingRequestData =
    HomeCallingRequestData(
        sessionId = sessionId,
        callerId = callerId,
        calleeId = calleeId,
        offer = offer?.let { HomeOfferAnswer(sdp = it.sdp, type = it.type, initiator = it.initiator) }
    )

// Bridges feature/home's local CallingRequestData back into feature/calling's shape, at the
// point where appInit hands the request off to the calling nav graph.
private fun HomeCallingRequestData.toCallingRequestData(): CallingRequestData =
    CallingRequestData(
        sessionId = sessionId,
        callerId = callerId,
        calleeId = calleeId,
        offer = offer?.let { OfferAnswer(sdp = it.sdp, type = it.type, initiator = it.initiator) }
    )

@Composable
fun SetUpNavigation(
    context: Any,
    platformContext: PlatformContext,
    deepLink: String = "",
    callSessionId: String? = null,
    callCallerId: String? = null,
    callCalleeId: String? = null,
    navigateToCallingScreenFromNotification: Boolean = false
) {
    val navController = rememberNavController()
    var selectedImage = ""
    var selectedUser: UserInstance? = null
    lateinit var selectedNew: HomeNewsInstance
    var currentDeepLink by remember { mutableStateOf(deepLink) }
    val coroutineScope = rememberCoroutineScope()
    val localImageLoaderValue = LocalImageLoader provides remember { generateImageLoader() }

    // Shared viewModels
    val homeViewModel: HomeViewModel = koinInject()
    val notificationsViewModel: NotificationsViewModel = koinInject()
    val accountViewModel: AccountViewModel = koinInject()
    val callViewModel: CallViewModel = koinInject()
    val shareViewModel: ShareViewModel = koinInject()
    val commentViewModel: CommentFeatureViewModel = koinInject()
     val syncLoadingViewModel: SyncLoadingViewModel = koinViewModel()
     val routerViewModel: RouterViewModel = koinViewModel()
    val commentScreenApi: CommentNavGraphImpl = koinInject()
    val signInViewModel: GoogleSignInHandler = koinInject()
    val authNavGraph: AuthNavGraph = koinInject()
    val homeNavGraph: HomeNavGraph = koinInject()
    val profileNavGraph: ProfileNavGraph = koinInject()
    val callingNavGraph: CallingNavGraph = koinInject()
    val uploadNewsfeedViewModel: UploadNewfeedViewModel = koinInject()
    val groupNavGraph: GroupNavGraph = koinInject()
    val securityNavGraph: SecurityNavGraph = koinInject()
    val notificationNavGraph: NotificationNavGraph = koinInject()
    val friendNavGraph: FriendNavGraph = koinInject()
    val searchNavGraph: SearchNavGraph = koinInject()
    val postInformationViewModel: PostInformationViewModel = koinViewModel()

    val syncDataUseCase: SyncDataUseCase = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    val securitySettingsRepository: SecuritySettingsRepository = koinInject()

    var updateNew: HomeNewsInstance? = null
    lateinit var relatedNew: HomeNewsInstance
    // Calling related shared states
    var callee by remember { mutableStateOf<UserInstance?>(null) }
    var caller by remember { mutableStateOf<UserInstance?>(null) }
    var sessionId by remember { mutableStateOf("") }
    var remoteOffer by remember { mutableStateOf<OfferAnswer?>(null) }
    var remoteVideoOffer by remember { mutableStateOf<OfferAnswer?>(null) }
    var navigateToVideoCallTrigger by remember { mutableStateOf(0) }

    // Incoming phone call request, bridged from feature/calling's CallViewModel into
    // feature/home's own local CallingRequestData shape (home has no Gradle dependency on calling).
    val phoneCallRequestStatus by callViewModel.phoneCallRequestStatus.collectAsState()
    val homeIncomingCallRequest = phoneCallRequestStatus?.let { it.toHomeCallingRequestData() }

    val isSyncLoading by syncLoadingViewModel.isSyncing.collectAsState()

    // Platform-specific helpers
    setupSignInLauncher(context, signInViewModel, platformContext)

    val snackBarHostState = remember { SnackbarHostState() }
    val networkStatus by platformContext.networkMonitor.isOnline.collectAsState(initial = null)

    // Track previous value
    var wasOffline by remember { mutableStateOf<Boolean?>(null) }

    //Group
    var selectedGroupId: String? = null


    // Force-logout from another device
    var forceLogoutDialogVisible by remember { mutableStateOf(false) }

    // Check persisted forced-logout status on startup (in case app was killed while LOGOUT)
    LaunchedEffect(homeViewModel.currentUserState) {
        val user = homeViewModel.currentUserState ?: return@LaunchedEffect
        val sessions = kotlinx.coroutines.withContext(Dispatchers.IO) {
            runCatching { securitySettingsRepository.fetchLoginHistoryList(user.uid) }.getOrElse { emptyList() }
        }
        val mySession = sessions.find { it.current }
        if (mySession != null) {
            if (mySession.status == "LOGOUT") {
                forceLogoutDialogVisible = true
                return@LaunchedEffect
            }
            // Start real-time listener for this session
            settingsRepository.observeSessionStatus(user.uid, mySession.sessionId) {
                forceLogoutDialogVisible = true
            }
        }
    }

    LaunchedEffect(networkStatus) {
        if (networkStatus != null) {
            // Figure out previous state (null on first run)
            val prev = wasOffline
            wasOffline = !networkStatus!!

            if (!networkStatus!!) {
                // Now offline → show persistent banner
                snackBarHostState.showSnackbar(
                    message = "You are offline. Check your internet!",
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
            } else {
                // Now online → only show if we were actually offline before
                if (prev == true) {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = "You are back online",
                        duration = SnackbarDuration.Short
                    )
                    //Has internet connection, sync data
                    if (homeViewModel.currentUser != null) {
                        //Show loading while sync data
                        syncLoadingViewModel.showSyncLoading()
                        //Sync data
                        syncDataUseCase(homeViewModel.currentUser!!.uid)
                        //Dismiss loading
                        syncLoadingViewModel.hideSyncLoading()
                    }
                } else {
                    // If we never showed the offline snackbar, just make sure nothing is stuck
                    snackBarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }

    LaunchedEffect(navigateToVideoCallTrigger) {
        if (navigateToVideoCallTrigger > 0) {
            navController.navigate(route = callingNavGraph.getVideoCallRoute())
        }
    }

    val listScreenNeedBottomBar = listOf(
        Screen.Home.route,
        Screen.Friend.route,
        Screen.Notification.route,
        Screen.Settings.route
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
            bottomBar = {
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                if (currentDestination in listScreenNeedBottomBar) {
                    val unreadNotificationCount = notificationsViewModel.listNotificationOfCurrentUser.count { !it.beRead }
                    BottomNavigationBar(
                        currentRoute = currentDestination,
                        onNavigate = { route -> navController.navigate(route) },
                        unreadNotificationCount = unreadNotificationCount,
                        onNavigateToUploadNews = {
                            navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            },
            snackbarHost = { UiUtils.MySnackBarHost(snackBarHostState, networkStatus) }
        ) { paddingValues ->
            NavHost(navController = navController, startDestination = "router") {
                composable(
                    route = "router"
                ) {
                    val startDestination by routerViewModel.startDestination.collectAsState()

                    LaunchedEffect(startDestination) {
                        startDestination?.let {
                            navController.navigate(it) {
                                popUpTo("router") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
                authNavGraph.registerRoutes(
                    navGraphBuilder = this,
                    navController = navController,
                    onUserSignedIn = { user -> routerViewModel.currentUser.value = user },
                    context = context,
                    platformContext = platformContext,
                    onNavigateToHome = { navController.navigate(route = homeNavGraph.getHomeRoute()) },
                    onNavigateToInformation = { navController.navigate(route = authNavGraph.getInformationRoute()) },
                    onNavigateToVerifyOTP = { navController.navigate(route = "VerifyOTPScreen") }
                )
                homeNavGraph.registerRoutes(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    navigateToCallingScreen = navigateToCallingScreenFromNotification,
                    platformContext = platformContext,
                    deepLink = currentDeepLink,
                    onNavigateToUploadNews = { new ->
                        updateNew = new
                        navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                    },
                    onNavigateToShowImageScreen = { image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())
                    },
                    onNavigateToSearch = { navController.navigate(route = searchNavGraph.getSearchRoute()) },
                    onNavigateToSignIn = {
                        //Clear email/password before navigate
                        signInViewModel.reset()
                        navController.navigate(route = AuthRouteNames.SignIn.SCREEN_NAME)
                    },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = profileNavGraph.getUserInformationRoute())
                    },
                    onNavigateToCommentScreen = { new ->
                        selectedNew = new
                        navController.navigate(route = CommentNavGraph.COMMENT_SCREEN_ROUTE)
                    },
                    onNavigateToCallingScreen = { callingRequestData ->
                        sessionId = callingRequestData.sessionId
                        remoteOffer = callingRequestData.toCallingRequestData().offer
                        caller =
                            (if (callingRequestData.callerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                callingRequestData.callerId
                            ))?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                        callee =
                            (if (callingRequestData.calleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                callingRequestData.calleeId
                            ))?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                        navController.navigate(route = callingNavGraph.getCallingRoute())
                    },
                    onObservePhoneCall = { callViewModel.observePhoneCall(homeViewModel.currentUser?.uid) },
                    incomingCallRequest = homeIncomingCallRequest,
                    onNavigateToCallingScreenWithUI = {
                        sessionId = callSessionId ?: ""
                        caller =
                            (if (callCallerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                callCallerId ?: ""
                            ))?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                        callee =
                            (if (callCalleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                callCalleeId ?: ""
                            ))?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                        navController.navigate(route = callingNavGraph.getCallingRoute())
                    },
                    onNavigateToPostInformation = {
                        val uri = Uri.parse(currentDeepLink)
                        val segments = uri.pathSegments
                        if (segments.isNotEmpty() && segments[0] == "news" && segments.size >= 2) {
                            val newsId = segments[1]
                            navController.navigate("news/$newsId") {
                                popUpTo("router") { inclusive = true }
                                launchSingleTop = true
                            }
                            // Clear deep link after handling to prevent repeated navigation
                            currentDeepLink = ""
                        }
                    },
                    onShareNews = { message, newToBeShared ->
                        //Basically, share news is similar to post news to newsfeed
                        if (homeViewModel.currentUser != null) {
                            shareViewModel.updateShareMessage(message)
                            shareViewModel.updateShareContent(newToBeShared)
                            shareViewModel.sharePost(homeViewModel.currentUser)
                        } else {
                            showToast("Cannot get your information to share now. Please try again!!!")
                        }
                    },
                    onNavigateToJoinGroup = {
                        val uri = Uri.parse(currentDeepLink)
                        val segments = uri.pathSegments
                        if (segments.isNotEmpty() && segments[0] == "groups" && segments.size >= 2) {
                            val groupId = segments[1]
                            navController.navigate("groups/$groupId") {
                                popUpTo("router") { inclusive = true }
                                launchSingleTop = true
                            }
                            // Clear deep link after handling to prevent repeated navigation
                            currentDeepLink = ""
                        }
                    }
                )
                homeNavGraph.registerUploadNewsfeedRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    context = context,
                    getUpdateNew = { updateNew }
                )
                composable(
                    route = ShowImage.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    ShowImage.ShowImageScreen(
                        selectedImage,
                        localImageLoaderValue = localImageLoaderValue,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.background),
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                searchNavGraph.registerRoutes(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = profileNavGraph.getUserInformationRoute())
                    },
                    onNavigateToShowImageScreen = { image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())
                    },
                    onNavigateToCommentScreen = { new ->
                        selectedNew = new
                        navController.navigate(route = CommentNavGraph.COMMENT_SCREEN_ROUTE)
                    },
                    onNavigateToUploadNewsFeed = { _ ->
                        navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                    }
                )
                profileNavGraph.registerUserInformationRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    context = context,
                    getSelectedUser = { selectedUser },
                    onNavigateToShowImageScreen = { image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())
                    },
                    onNavigateToCallingScreen = { user ->
                        if (user != null) {
                            caller = homeViewModel.currentUser?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                            callee = user
                            navController.navigate(route = callingNavGraph.getCallingRoute())
                        }
                    },
                    onNavigateToCommentScreen = { new ->
                        selectedNew = new
                        navController.navigate(route = CommentNavGraph.COMMENT_SCREEN_ROUTE)
                    },
                    onNavigateToUploadNewsfeed = { new ->
                        updateNew = new
                        navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                    }
                )
                composable(
                    route = CommentNavGraph.COMMENT_SCREEN_ROUTE,
                     enterTransition = DefaultNavAnimations.enter,
                     popEnterTransition = DefaultNavAnimations.popEnter,
                     exitTransition = DefaultNavAnimations.exit,
                     popExitTransition = DefaultNavAnimations.popExit
                 ) {
                    commentScreenApi.renderCommentScreen(
                        paddingValues = paddingValues,
                         modifier = Modifier
                             .fillMaxSize()
                             .background(color = MaterialTheme.colorScheme.background),
                         platform = platformContext,
                         localImageLoaderValue = localImageLoaderValue,
                         showCloseIcon = true,
                         commentViewModel = commentViewModel,
                         currentUser = homeViewModel.currentUser!!.toHomeUserDto().toAppInitUserDto().toCommentUserDto().toCommentUser(),
                         selectedNewId = selectedNew.id,
                         selectedNewPosterId = selectedNew.posterId,
                         onNavigateToShowImageScreen = { image ->
                             selectedImage = image
                             navController.navigate(route = ShowImage.getScreenName())
                         },
                         onNavigateToUserInformation = { user ->
                             selectedUser = user?.toCommentUserDto()?.toAppInitUserDto()?.toSearchUser()
                             navController.navigate(route = profileNavGraph.getUserInformationRoute())
                         },
                         onNavigateToHomeScreen = { numberOfComments ->
                             homeViewModel.addCommentCountData(selectedNew.id, numberOfComments)
                             navController.popBackStack()
                         }
                    )
                 }
                friendNavGraph.registerRoutes(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = profileNavGraph.getUserInformationRoute())
                    }
                )
                notificationNavGraph.registerRoutes(
                    navGraphBuilder = this,
                    navController = navController,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    onNavigateToPostInformation = { new ->
                        relatedNew = new
                        navController.navigate(route = PostInformation.getScreenName())
                    },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = profileNavGraph.getUserInformationRoute())
                    },
                    onNavigateToGroupDetails = { groupId ->
                        selectedGroupId = groupId
                        navController.navigate(route = groupNavGraph.getGroupDetailsRoute())
                    },
                    onNavigateBackFromConfigs = {
                        navController.popBackStack()
                    }
                )
                composable(
                    route = Screen.Settings.route,
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    Settings.SettingsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        paddingValues = paddingValues,
                        onNavigateToSignIn = {
                            //Clear email/password before navigate
                            signInViewModel.reset()
                            navController.navigate(route = AuthRouteNames.SignIn.SCREEN_NAME)
                        },
                        onNavigateToProfileInformation = {
                            selectedUser = homeViewModel.currentUser?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                            navController.navigate(route = profileNavGraph.getPersonalInformationRoute())
                        },
                        onNavigateToGroupScreen = {
                            navController.navigate(route = groupNavGraph.getGroupRoute())
                        },
                        onNavigateToPrivacyScreen = {
                            navController.navigate(route = securityNavGraph.getPrivacyRoute())
                        },
                        onNavigateToSecuritySettingsScreen = {
                            navController.navigate(route = securityNavGraph.getSecuritySettingsRoute())
                        },
                        onNavigateToNotificationConfigsScreen = {
                            navController.navigate(route = notificationNavGraph.getNotificationConfigsRoute())
                        }
                    )
                }
                composable(
                    route = PostInformation.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    PostInformation.PostInformationScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        platformContext,
                        localImageLoaderValue = localImageLoaderValue,
                        relatedNew,
                        onNavigateToShowImageScreen = { image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        },
                        onNavigateToUserInformation = { user ->
                            selectedUser = user?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                            navController.navigate(route = profileNavGraph.getUserInformationRoute())
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        homeViewModel,
                        postInformationViewModel = postInformationViewModel,
                        onNavigateToUploadNews = { new ->
                            updateNew = new
                            navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                        },
                        onShareNews = { message, newToBeShared ->
                            //Basically, share news is similar to post news to newsfeed
                            if (homeViewModel.currentUser != null) {
                                shareViewModel.updateShareMessage(message)
                                shareViewModel.updateShareContent(newToBeShared)
                                shareViewModel.sharePost(homeViewModel.currentUser)
                            } else {
                                showToast("Cannot get your information to share now. Please try again!!!")
                            }
                        }
                    )
                }
                callingNavGraph.registerRoutes(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    localImageLoaderValue = localImageLoaderValue,
                    getCallee = { callee },
                    getCaller = { caller },
                    getSessionId = { sessionId },
                    getRemoteOffer = { remoteOffer },
                    getRemoteVideoOffer = { remoteVideoOffer },
                    navigateToCallingScreenFromNotification = navigateToCallingScreenFromNotification,
                    onSetRemoteVideoOffer = { remoteVideoOffer = it },
                    onSetSessionId = { sessionId = it },
                    onNavigateToVideoCall = { navigateToVideoCallTrigger++ },
                    onStopCallAndNavigateBack = {
                        callViewModel.resetCallEvent()
                    },
                    getHomeRoute = { homeNavGraph.getHomeRoute() }
                )
                composable(
                    route = "news/{newsId}"
                ) { backStackEntry ->
                    val newsId = backStackEntry.savedStateHandle.get<String>("newsId") ?: return@composable

                    val newsState by postInformationViewModel.newFromDeepLink.collectAsState()

                    LaunchedEffect(newsId) {
                        postInformationViewModel.requestFindNewById(newsId)
                    }

                    if (newsState != null) {
                        PostInformation.PostInformationScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            platformContext,
                            localImageLoaderValue = localImageLoaderValue,
                            newsState!!,
                            onNavigateToShowImageScreen = { image ->
                                selectedImage = image
                                navController.navigate(route = ShowImage.getScreenName())
                            },
                            onNavigateToUserInformation = { user ->
                                selectedUser = user?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser()
                                navController.navigate(route = profileNavGraph.getUserInformationRoute())
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            homeViewModel,
                            postInformationViewModel = postInformationViewModel,
                            onNavigateToUploadNews = { new ->
                                updateNew = new
                                navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                            },
                            onShareNews = { message, newToBeShared ->
                                //Basically, share news is similar to post news to newsfeed
                                if (homeViewModel.currentUser != null) {
                                    shareViewModel.updateShareMessage(message)
                                    shareViewModel.updateShareContent(newToBeShared)
                                    shareViewModel.sharePost(homeViewModel.currentUser)
                                } else {
                                    showToast("Cannot get your information to share now. Please try again!!!")
                                }
                            }
                        )
                    }
                }
                groupNavGraph.registerGroupRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    paddingValues = paddingValues,
                    getSelectedGroupId = { selectedGroupId },
                    onNavigateToCreateGroup = {
                        navController.navigate(route = groupNavGraph.getCreateGroupRoute())
                    },
                    onNavigateToExploreGroup = {
                        navController.navigate(route = groupNavGraph.getExploreGroupRoute())
                    },
                    onNavigateToSelectGroup = {
                        navController.navigate(route = groupNavGraph.getSelectGroupRoute())
                    }
                )
                groupNavGraph.registerCreateGroupRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    context = context,
                    paddingValues = paddingValues,
                    homeViewModel = homeViewModel,
                    onCreateGroupSuccess = { groupId ->
                        selectedGroupId = groupId
                        navController.navigate(route = groupNavGraph.getGroupDetailsRoute()) {
                            popUpTo(groupNavGraph.getCreateGroupRoute()) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
                groupNavGraph.registerExploreGroupRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    homeViewModel = homeViewModel,
                    onNavigateToGroupDetails = { groupId ->
                        selectedGroupId = groupId
                        navController.navigate(route = groupNavGraph.getGroupDetailsRoute())
                    }
                )
                groupNavGraph.registerGroupDetailsRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    context = context,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    homeViewModel = homeViewModel,
                    getSelectedGroupId = { selectedGroupId },
                    onNavigateToShowImageScreen = { image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())
                    },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = profileNavGraph.getUserInformationRoute())
                    },
                    onNavigateToUploadNewsfeed = { new ->
                        updateNew = new
                        uploadNewsfeedViewModel.updateGroupId(selectedGroupId!!)
                        navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                    },
                    onNavigateToCommentScreen = { new ->
                        selectedNew = new
                        navController.navigate(route = CommentNavGraph.COMMENT_SCREEN_ROUTE)
                    },
                    onNavigateToInviteMember = {
                        navController.navigate(route = groupNavGraph.getInviteMemberRoute())
                    },
                    onLeaveGroup = {
                        navController.popBackStack()
                        if (homeViewModel.currentUser != null && selectedGroupId != null) {
                            homeViewModel.currentUser!!.groups.remove(selectedGroupId!!)
                        }
                    },
                    onManageMembers = { groupId ->
                        selectedGroupId = groupId
                        navController.navigate(route = groupNavGraph.getManageMembersRoute())
                    },
                    onCreatePoll = {
                        navController.navigate(route = groupNavGraph.getCreatePollRoute())
                    }
                )
                groupNavGraph.registerSelectGroupRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    homeViewModel = homeViewModel,
                    onNavigateToCreateGroup = {
                        navController.navigate(groupNavGraph.getCreateGroupRoute())
                    },
                    onNavigateToSelectedGroup = { groupId ->
                        selectedGroupId = groupId
                        navController.navigate(route = groupNavGraph.getGroupDetailsRoute())
                    }
                )
                groupNavGraph.registerInviteMemberRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    homeViewModel = homeViewModel,
                    getSelectedGroupId = { selectedGroupId }
                )
                groupNavGraph.registerGroupDetailsDeepLinkRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    context = context,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    homeViewModel = homeViewModel,
                    getSelectedGroupId = { selectedGroupId },
                    onNavigateToShowImageScreen = { image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())
                    },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = profileNavGraph.getUserInformationRoute())
                    },
                    onNavigateToUploadNewsfeed = { new ->
                        updateNew = new
                        if (selectedGroupId != null) uploadNewsfeedViewModel.updateGroupId(selectedGroupId!!)
                        navController.navigate(route = homeNavGraph.getUploadNewsfeedRoute())
                    },
                    onNavigateToCommentScreen = { new ->
                        selectedNew = new
                        navController.navigate(route = CommentNavGraph.COMMENT_SCREEN_ROUTE)
                    },
                    onNavigateToInviteMember = {
                        navController.navigate(route = groupNavGraph.getInviteMemberRoute())
                    },
                    onLeaveGroup = {
                        navController.popBackStack()
                        if (homeViewModel.currentUser != null && selectedGroupId != null) {
                            homeViewModel.currentUser!!.groups.remove(selectedGroupId!!)
                        }
                    },
                    onManageMembers = { groupId ->
                        selectedGroupId = groupId
                        navController.navigate(route = groupNavGraph.getManageMembersRoute())
                    },
                    onCreatePoll = {
                        navController.navigate(route = groupNavGraph.getCreatePollRoute())
                    }
                )
                groupNavGraph.registerManageMembersRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    homeViewModel = homeViewModel,
                    getSelectedGroupId = { selectedGroupId },
                    onNavigateToInviteMembers = {
                        navController.navigate(route = groupNavGraph.getInviteMemberRoute())
                    },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = profileNavGraph.getUserInformationRoute())
                    },
                    onNavigateToSelectGroup = {
                        if (selectedGroupId != null) homeViewModel.currentUser!!.groups.remove(selectedGroupId!!)
                        navController.navigate(route = groupNavGraph.getSelectGroupRoute()) {
                            popUpTo(groupNavGraph.getSelectGroupRoute()) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )

                securityNavGraph.registerRoutes(
                    navGraphBuilder = this,
                    navController = navController,
                    localImageLoaderValue = localImageLoaderValue,
                    getCurrentUser = { homeViewModel.currentUser?.toHomeUserDto()?.toAppInitUserDto()?.toSearchUser() ?: routerViewModel.currentUser.value },
                    getHomeRoute = { homeNavGraph.getHomeRoute() },
                    onNavigateToForgotPassword = {
                        navController.navigate(AuthRouteNames.ForgotPassword.SCREEN_NAME)
                    },
                    onNavigateToSignIn = {
                        navController.navigate(AuthRouteNames.SignIn.SCREEN_NAME) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )


                profileNavGraph.registerPersonalInformationRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    paddingValues = paddingValues,
                    context = context,
                    getSelectedUser = { selectedUser }
                )

                groupNavGraph.registerCreatePollRoute(
                    navGraphBuilder = this,
                    navController = navController,
                    homeViewModel = homeViewModel,
                    getSelectedGroupId = { selectedGroupId }
                )
            }
            if (isSyncLoading) {
                GifLoading.GifLoadingScreen(
                    localImageLoaderValue,
                    "loading_gif",
                    "Syncing your data..."
                )
            }

            // Forced-logout dialog: shown when another device logged out this session
            if (forceLogoutDialogVisible) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { /* non-dismissible */ },
                    title = { androidx.compose.material3.Text("Session Ended") },
                    text = {
                        androidx.compose.material3.Text(
                            "Your account has been logged out from this device by another session. Please sign in again to continue."
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                forceLogoutDialogVisible = false
                                settingsRepository.stopObserveSessionStatus()
                                securitySettingsRepository.clearLocalSession()
                                accountViewModel.clearAccountInStorage()
                                homeViewModel.clearLocalData()
                                signInViewModel.reset()
                                navController.navigate(AuthRouteNames.SignIn.SCREEN_NAME) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        ) {
                            androidx.compose.material3.Text("Sign In")
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
            }
        }
    }
}
