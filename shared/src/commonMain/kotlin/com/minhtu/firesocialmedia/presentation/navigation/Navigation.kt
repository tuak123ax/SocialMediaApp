package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eygraber.uri.Uri
import com.minhtu.firesocialmedia.di.AppModule
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.di.ViewModelProvider
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SharedCallData
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.home.deeplinks.DeepLinksData
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.platformViewModel
import com.minhtu.firesocialmedia.platform.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.platform.setupSignInLauncher
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.calling.audiocall.Calling
import com.minhtu.firesocialmedia.presentation.calling.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.presentation.calling.videocall.VideoCall
import com.minhtu.firesocialmedia.presentation.calling.videocall.VideoCallViewModel
import com.minhtu.firesocialmedia.presentation.comment.Comment
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.presentation.home.Home
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.information.Information
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.loading.GifLoading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.Screen
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.Friend
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.NotificationViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.CreateGroup
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.CreateGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ExploreGroup
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ExploreGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.Group
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.GroupDetails
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.GroupDetailsViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.InviteMember
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.InviteMemberViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembers
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembersViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.SelectGroup
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.SelectGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.notificationconfigs.NotificationConfigs
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.notificationconfigs.NotificationConfigsViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.personal.PersonalInformation
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.personal.PersonalInformationViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.privacy.Privacy
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.SecuritySettings
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.SecuritySettingsViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.changepassword.ChangePassword
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.changepassword.ChangePasswordViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.loginhistory.LoginHistory
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.loginhistory.LoginHistoryViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.BackUpCode
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.BackUpCodeViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.TwoFA
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.TwoFAViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.TwoFactorEnabled
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.TwoFactorEnabledViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.VerifyOTP
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.VerifyOTPViewModel
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformation
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformationViewModel
import com.minhtu.firesocialmedia.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.showimage.ShowImage
import com.minhtu.firesocialmedia.presentation.showimage.ShowImageViewModel
import com.minhtu.firesocialmedia.presentation.signin.SignIn
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.presentation.signup.SignUp
import com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewsfeed
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformation
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.BottomNavigationBar
import com.seiko.imageloader.LocalImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SetUpNavigation(context: Any, platformContext: PlatformContext) {
    val navController = rememberNavController()
    val navigationHandler =
        com.minhtu.firesocialmedia.platform.rememberNavigationHandler(navController)
    var selectedImage = ""
    var selectedUser: UserInstance? = null
    lateinit var selectedNew: NewsInstance
    val coroutineScope = rememberCoroutineScope()
    val localImageLoaderValue = LocalImageLoader provides remember { generateImageLoader() }

    // Shared viewModels
    val signUpViewModel: SignUpViewModel =
        platformViewModel { ViewModelProvider.createSignUpViewModel(platformContext) }
    val signInViewModel: SignInViewModel =
        platformViewModel { ViewModelProvider.createSignInViewModel(platformContext) }
    val loadingViewModel: LoadingViewModel =
        platformViewModel { ViewModelProvider.createLoadingViewModel() }
    val forgotPasswordViewModel: ForgotPasswordViewModel =
        platformViewModel { ViewModelProvider.createForgotPasswordViewModel(platformContext) }
    val commentViewModel: CommentViewModel =
        platformViewModel { ViewModelProvider.createCommentViewModel(platformContext) }
    val searchViewModel: SearchViewModel =
        platformViewModel { ViewModelProvider.createSearchViewModel() }
    val uploadNewsfeedViewModel: UploadNewfeedViewModel =
        platformViewModel { ViewModelProvider.createUploadNewfeedViewModel(platformContext) }
    val informationViewModel: InformationViewModel =
        platformViewModel { ViewModelProvider.createInformationViewModel(platformContext) }
    val friendViewModel: FriendViewModel =
        platformViewModel { ViewModelProvider.createFriendViewModel(platformContext) }
    val userInformationViewModel: UserInformationViewModel =
        platformViewModel { ViewModelProvider.createUserInformationViewModel(platformContext) }
    val showImageViewModel: ShowImageViewModel =
        platformViewModel { ViewModelProvider.createShowImageViewModel(platformContext) }
    val notificationViewModel: NotificationViewModel =
        platformViewModel { ViewModelProvider.createNotificationViewModel(platformContext) }
    val homeViewModel: HomeViewModel =
        platformViewModel { ViewModelProvider.createHomeViewModel(platformContext) }
    val postInformationViewModel: PostInformationViewModel =
        platformViewModel { ViewModelProvider.createPostInformationViewModel(platformContext) }
    val createGroupViewModel: CreateGroupViewModel =
        platformViewModel { ViewModelProvider.createCreateGroupViewModel(platformContext) }
    val groupDetailsViewModel: GroupDetailsViewModel =
        platformViewModel { ViewModelProvider.createGroupDetailsViewModel(platformContext) }
    val syncDataUseCase =
        AppModule.provideSyncDataUseCase(AppModule.provideCommonDbRepository(platformContext))
    val selectGroupViewModel: SelectGroupViewModel =
        platformViewModel { ViewModelProvider.createSelectGroupViewModel(platformContext) }
    val inviteMemberViewModel: InviteMemberViewModel =
        platformViewModel { ViewModelProvider.createInviteMemberViewModel(platformContext) }
    val manageMembersViewModel: ManageMembersViewModel =
        platformViewModel { ViewModelProvider.createManageMembersViewModel(platformContext) }
    val exploreGroupViewModel: ExploreGroupViewModel =
        platformViewModel { ViewModelProvider.createExploreGroupViewModel(platformContext) }
    val notificationConfigsViewModel: NotificationConfigsViewModel =
        platformViewModel { ViewModelProvider.createNotificationConfigsViewModel(platformContext) }
    val changePasswordViewModel: ChangePasswordViewModel =
        platformViewModel { ViewModelProvider.createChangePasswordViewModel(platformContext) }

    var updateNew: NewsInstance? = null
    lateinit var relatedNew: NewsInstance
    // Calling related shared states
    var callee by remember { mutableStateOf<UserInstance?>(null) }
    var caller by remember { mutableStateOf<UserInstance?>(null) }
    var sessionId by remember { mutableStateOf("") }
    var remoteOffer by remember { mutableStateOf<OfferAnswer?>(null) }
    var remoteVideoOffer by remember { mutableStateOf<OfferAnswer?>(null) }
    var navigateToVideoCallTrigger by remember { mutableStateOf(0) }
    val callingViewModel: CallingViewModel =
        platformViewModel { ViewModelProvider.createCallingViewModel(platformContext) }
    val videoCallViewModel: VideoCallViewModel =
        platformViewModel { ViewModelProvider.createVideoCallViewModel(platformContext) }

    //Settings
    val securitySettingsViewModel: SecuritySettingsViewModel =
        platformViewModel { ViewModelProvider.createSecuritySettingsViewModel(platformContext) }

    //Personal information
    val personalInformationViewModel : PersonalInformationViewModel =
        platformViewModel { ViewModelProvider.createPersonalInformationViewModel(platformContext) }

    val isSyncLoading by loadingViewModel.syncLoading.collectAsState()

    // Platform-specific helpers
    setupSignInLauncher(context, signInViewModel, platformContext)

    val snackBarHostState = remember { SnackbarHostState() }
    val networkStatus by platformContext.networkMonitor.isOnline.collectAsState(initial = null)

    // Track previous value
    var wasOffline by remember { mutableStateOf<Boolean?>(null) }

    //Group
    var selectedGroup: GroupInstance? = null

    //2FA
    val twoFAViewModel: TwoFAViewModel =
        platformViewModel { ViewModelProvider.create2FAViewModel(platformContext) }
    var localSecret = ""
    val verifyOTPViewModel: VerifyOTPViewModel =
        platformViewModel { ViewModelProvider.createVerifyOTPViewModel(platformContext) }
    var backupCode = ""
    val twoFactorEnabledViewModel: TwoFactorEnabledViewModel =
        platformViewModel { ViewModelProvider.createTwoFactorEnabledViewModel(platformContext) }
    val backUpCodeViewModel: BackUpCodeViewModel =
        platformViewModel { ViewModelProvider.createBackUpCodeViewModel(platformContext) }
    var isEnable2FAFlow = false
    val routerViewModel: RouterViewModel =
        platformViewModel { ViewModelProvider.createRouterViewModel(platformContext) }

    //Login History
    val loginHistoryViewModel: LoginHistoryViewModel =
        platformViewModel { ViewModelProvider.createLoginHistoryViewModel(platformContext) }

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
                        loadingViewModel.showSyncLoading()
                        //Sync data
                        syncDataUseCase.invoke(homeViewModel.currentUser!!.uid)
                        //Dismiss loading
                        loadingViewModel.hideSyncLoading()
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
            navController.navigate(route = VideoCall.getScreenName())
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
                    BottomNavigationBar(
                        currentRoute = currentDestination,
                        onNavigate = { route -> navController.navigate(route) },
                        homeViewModel = homeViewModel,
                        onNavigateToUploadNews = {
                            navController.navigate(route = UploadNewsfeed.getScreenName())
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
                composable(
                    route = SignIn.getScreenName()
                ) {
                    SignIn.SignInScreen(
                        signInViewModel,
                        loadingViewModel,
                        routerViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        onNavigateToSignUpScreen = { navController.navigate(route = SignUp.getScreenName()) },
                        onNavigateToHomeScreen = { navController.navigate(route = Home.getScreenName()) },
                        onNavigateToInformationScreen = { navController.navigate(route = Information.getScreenName()) },
                        onNavigateToForgotPasswordScreen = { navController.navigate(route = ForgotPassword.getScreenName()) },
                        onNavigateToVerifyOTP = { navController.navigate(route = VerifyOTP.getScreenName()) }
                    )
                }
                composable(
                    route = SignUp.getScreenName()
                ) {
                    SignUp.SignUpScreen(
                        signUpViewModel,
                        loadingViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        onNavigateToSignInScreen = {
                            navController.navigate(SignIn.getScreenName()) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToInformationScreen = { navController.navigate(route = Information.getScreenName()) }
                    )
                }
                composable(
                    route = Information.getScreenName()
                ) {
                    val picker = rememberPlatformImagePicker(
                        context = context,
                        onImagePicked = { uri -> informationViewModel.updateAvatar(uri) },
                        onVideoPicked = {}
                    )
                    Information.InformationScreen(
                        platform = platformContext,
                        imagePicker = picker,
                        signUpViewModel = signUpViewModel,
                        informationViewModel = informationViewModel,
                        loadingViewModel = loadingViewModel,
                        onNavigateToHomeScreen = { navController.navigate(route = Home.getScreenName()) }
                    )
                }
                composable(
                    route = Home.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    Home.HomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        homeViewModel,
                        loadingViewModel,
                        SharedCallData.navigateToCallingScreenFromNotification,
                        paddingValues = paddingValues,
                        localImageLoaderValue = localImageLoaderValue,
                        onNavigateToUploadNews = { new ->
                            updateNew = new
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        },
                        onNavigateToShowImageScreen = { image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        },
                        onNavigateToSearch = { navController.navigate(route = Search.getScreenName()) },
                        onNavigateToSignIn = {
                            //Clear email/password before navigate
                            signInViewModel.reset()
                            navController.navigate(route = SignIn.getScreenName())
                        },
                        onNavigateToUserInformation = { user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateToCommentScreen = { new ->
                            selectedNew = new
                            navController.navigate(route = Comment.getScreenName())
                        },
                        onNavigateToCallingScreen = { callingRequestData ->
                            sessionId = callingRequestData.sessionId
                            remoteOffer = callingRequestData.offer
                            caller =
                                if (callingRequestData.callerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                    callingRequestData.callerId
                                )
                            callee =
                                if (callingRequestData.calleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                    callingRequestData.calleeId
                                )
                            navController.navigate(route = Calling.getScreenName())
                        },
                        onNavigateToCallingScreenWithUI = {
                            sessionId = SharedCallData.sessionId
                            caller =
                                if (SharedCallData.callerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                    SharedCallData.callerId
                                )
                            callee =
                                if (SharedCallData.calleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(
                                    SharedCallData.calleeId
                                )
                            navController.navigate(route = Calling.getScreenName())
                        },
                        onNavigateToPostInformation = {
                            val uri = Uri.parse(DeepLinksData.deepLink)
                            val segments = uri.pathSegments
                            if (segments.isNotEmpty() && segments[0] == "news" && segments.size >= 2) {
                                val newsId = segments[1]
                                navController.navigate("news/$newsId") {
                                    popUpTo("router") { inclusive = true }
                                    launchSingleTop = true
                                }
                                // Clear deep link after handling to prevent repeated navigation
                                DeepLinksData.deepLink = ""
                            }
                        },
                        onShareNews = { message, newToBeShared ->
                            //Basically, share news is similar to post news to newsfeed
                            if (homeViewModel.currentUser != null) {
                                homeViewModel.updateShareMessage(message)
                                homeViewModel.updateShareContent(newToBeShared)
                                homeViewModel.sharePost(homeViewModel.currentUser!!)
                            } else {
                                showToast("Cannot get your information to share now. Please try again!!!")
                            }
                        },
                        onNavigateToJoinGroup = {
                            val uri = Uri.parse(DeepLinksData.deepLink)
                            val segments = uri.pathSegments
                            if (segments.isNotEmpty() && segments[0] == "groups" && segments.size >= 2) {
                                val groupId = segments[1]
                                navController.navigate("groups/$groupId") {
                                    popUpTo("router") { inclusive = true }
                                    launchSingleTop = true
                                }
                                // Clear deep link after handling to prevent repeated navigation
                                DeepLinksData.deepLink = ""
                            }
                        }
                    )
                }
                composable(
                    route = UploadNewsfeed.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    val picker = rememberPlatformImagePicker(
                        context = context,
                        onImagePicked = { uri -> uploadNewsfeedViewModel.updateImage(uri) },
                        onVideoPicked = { uri -> uploadNewsfeedViewModel.updateVideo(uri) }
                    )
                    UploadNewsfeed.UploadNewsfeedScreen(
                        paddingValues,
                        imagePicker = picker,
                        localImageLoaderValue,
                        homeViewModel = homeViewModel,
                        uploadNewsfeedViewModel = uploadNewsfeedViewModel,
                        loadingViewModel = loadingViewModel,
                        updateNew = updateNew,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
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
                        showImageViewModel = showImageViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.background),
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = Search.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    coroutineScope.launch(Dispatchers.IO) {
                        delay(700)
                        //Reset search text
                        searchViewModel.updateQuery("")
                    }
                    Search.SearchScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.background),
                        paddingValues,
                        searchViewModel,
                        homeViewModel,
                        localImageLoaderValue = localImageLoaderValue,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToUserInformation = { user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateToShowImageScreen = { image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        },
                        onNavigateToCommentScreen = { new ->
                            selectedNew = new
                            navController.navigate(route = Comment.getScreenName())
                        },
                        onNavigateToUploadNewsFeed = { _ ->
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        }
                    )
                }
                composable(
                    route = UserInformation.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    val picker = rememberPlatformImagePicker(
                        context = context,
                        onImagePicked = { uri -> userInformationViewModel.updateCover(uri) },
                        onVideoPicked = {}
                    )
                    UserInformation.UserInformationScreen(
                        imagePicker = picker,
                        user = selectedUser,
                        isCurrentUser = selectedUser == homeViewModel.currentUser,
                        paddingValues = paddingValues,
                        localImageLoaderValue = localImageLoaderValue,
                        homeViewModel = homeViewModel,
                        friendViewModel = friendViewModel,
                        userInformationViewModel = userInformationViewModel,
                        loadingViewModel = loadingViewModel,
                        onNavigateToShowImageScreen = { image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        },
                        onNavigateBack = {
                            userInformationViewModel.resetOldData()
                            navController.popBackStack()
                        },
                        onNavigateToUploadNewsfeed = { new ->
                            updateNew = new
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        },
                        onNavigateToCallingScreen = { user ->
                            if (user != null) {
                                caller = homeViewModel.currentUser
                                callee = user
                                navController.navigate(route = Calling.getScreenName())
                            }
                        },
                        onNavigateToCommentScreen = { new ->
                            selectedNew = new
                            navController.navigate(route = Comment.getScreenName())
                        }
                    )
                }
                composable(
                    route = Comment.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    Comment.CommentScreen(
                        paddingValues,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.background),
                        platform = platformContext,
                        localImageLoaderValue = localImageLoaderValue,
                        showCloseIcon = true,
                        commentViewModel = commentViewModel,
                        currentUser = homeViewModel.currentUser!!,
                        selectedNew = selectedNew,
                        onNavigateToShowImageScreen = { image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        },
                        onNavigateToUserInformation = { user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        }
                    ) { numberOfComments ->
                        homeViewModel.addCommentCountData(selectedNew.id, numberOfComments)
                        navController.popBackStack()
                    }
                }
                composable(
                    route = ForgotPassword.getScreenName()
                ) {
                    ForgotPassword.ForgotPasswordScreen(
                        forgotPasswordViewModel,
                        loadingViewModel,
                        onNavigateToSignInScreen = {
                            navController.navigate(SignIn.getScreenName()) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = Screen.Friend.route,
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    coroutineScope.launch(Dispatchers.IO) {
                        delay(700)
                        //Reset search text
                        searchViewModel.updateQuery("")
                    }
                    Friend.FriendScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        paddingValues = paddingValues,
                        localImageLoaderValue = localImageLoaderValue,
                        searchViewModel,
                        homeViewModel = homeViewModel,
                        friendViewModel,
                        onNavigateToUserInformation = { user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateToShowImageScreen = { image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        }
                    )
                }
                composable(
                    route = Screen.Notification.route,
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    Notification.NotificationScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        paddingValues = paddingValues,
                        localImageLoaderValue = localImageLoaderValue,
                        searchViewModel = searchViewModel,
                        homeViewModel = homeViewModel,
                        notificationViewModel = notificationViewModel,
                        loadingViewModel = loadingViewModel,
                        onNavigateToPostInformation = { new ->
                            relatedNew = new
                            navController.navigate(route = PostInformation.getScreenName())
                        },
                        onNavigateToUserInformation = { user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateToGroupDetails = { groupId ->
                            selectedGroup = GroupInstance(id = groupId)
                            navController.navigate(route = GroupDetails.getScreenName())
                        }
                    )
                }
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
                        homeViewModel = homeViewModel,
                        onNavigateToSignIn = {
                            //Clear email/password before navigate
                            signInViewModel.reset()
                            navController.navigate(route = SignIn.getScreenName())
                        },
                        onNavigateToProfileInformation = {
                            selectedUser = homeViewModel.currentUser
                            navController.navigate(route = PersonalInformation.getScreenName())
                        },
                        onNavigateToGroupScreen = {
                            navController.navigate(route = Group.getScreenName())
                        },
                        onNavigateToPrivacyScreen = {
                            navController.navigate(route = Privacy.getScreenName())
                        },
                        onNavigateToSecuritySettingsScreen = {
                            navController.navigate(route = SecuritySettings.getScreenName())
                        },
                        onNavigateToNotificationConfigsScreen = {
                            navController.navigate(route = NotificationConfigs.getScreenName())
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
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        homeViewModel,
                        commentViewModel,
                        postInformationViewModel,
                        onNavigateToUploadNews = { new ->
                            updateNew = new
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        },
                        onShareNews = { message, newToBeShared ->
                            //Basically, share news is similar to post news to newsfeed
                            if (homeViewModel.currentUser != null) {
                                homeViewModel.updateShareMessage(message)
                                homeViewModel.updateShareContent(newToBeShared)
                                homeViewModel.sharePost(homeViewModel.currentUser!!)
                            } else {
                                showToast("Cannot get your information to share now. Please try again!!!")
                            }
                        }
                    )
                }
                composable(
                    route = Calling.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    if (caller != null && callee != null) {
                        Calling.CallingScreen(
                            localImageLoaderValue = localImageLoaderValue,
                            sessionId,
                            callee!!,
                            caller!!,
                            homeViewModel.currentUser,
                            remoteOffer,
                            SharedCallData.navigateToCallingScreenFromNotification,
                            callingViewModel,
                            homeViewModel,
                            navigationHandler,
                            onStopCallAndNavigateBack = {
                                if (navigationHandler.getCurrentRoute() != Home.getScreenName()) {
                                    navigationHandler.navigateBack()
                                }
                                homeViewModel.resetCallEvent()
                            },
                            onNavigateToVideoCall = { sessionID, videoOffer ->
                                videoCallViewModel.setPendingVideoCallParams(sessionID, videoOffer)
                                sessionId = sessionID
                                remoteVideoOffer = videoOffer
                                navigateToVideoCallTrigger++
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    } else {
                        showToast("Cannot get caller and callee information. Cannot show calling screen!")
                        navController.popBackStack()
                    }
                }
                composable(
                    route = VideoCall.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    VideoCall.VideoCallScreen(
                        sessionId,
                        caller,
                        callee,
                        homeViewModel.currentUser?.uid,
                        remoteVideoOffer,
                        videoCallViewModel,
                        loadingViewModel,
                        onNavigateBack = {
                            navigationHandler.navigateBack()
                        }
                    )
                }
                composable(
                    route = "news/{newsId}"
                ) { backStackEntry ->
                    val newsId = backStackEntry.arguments?.getString("newsId")!!

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
                                selectedUser = user
                                navController.navigate(route = UserInformation.getScreenName())
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            homeViewModel,
                            commentViewModel,
                            postInformationViewModel,
                            onNavigateToUploadNews = { new ->
                                updateNew = new
                                navController.navigate(route = UploadNewsfeed.getScreenName())
                            },
                            onShareNews = { message, newToBeShared ->
                                //Basically, share news is similar to post news to newsfeed
                                if (homeViewModel.currentUser != null) {
                                    homeViewModel.updateShareMessage(message)
                                    homeViewModel.updateShareContent(newToBeShared)
                                    homeViewModel.sharePost(homeViewModel.currentUser!!)
                                } else {
                                    showToast("Cannot get your information to share now. Please try again!!!")
                                }
                            }
                        )
                    }
                }
                composable(
                    route = Group.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    if (homeViewModel.currentUser != null) {
                        Group.GroupScreen(
                            homeViewModel.currentUser!!,
                            paddingValues,
                            onNavigateToCreateGroupScreen = {
                                navController.navigate(route = CreateGroup.getScreenName())
                            },
                            onNavigateToExploreGroupScreen = {
                                navController.navigate(route = ExploreGroup.getScreenName())
                            },
                            onNavigateToSelectGroupScreen = {
                                navController.navigate(route = SelectGroup.getScreenName())
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        showToast("Cannot open group screen. Retry later!!!")
                        navController.popBackStack()
                    }
                }
                composable(
                    route = CreateGroup.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    val picker = rememberPlatformImagePicker(
                        context = context,
                        onImagePicked = { uri -> createGroupViewModel.updateAvatar(uri) },
                        onVideoPicked = {}
                    )
                    if (homeViewModel.currentUser != null) {
                        CreateGroup.CreateGroupScreen(
                            paddingValues,
                            createGroupViewModel,
                            loadingViewModel,
                            picker,
                            homeViewModel.currentUser!!,
                            onCreateGroupSuccess = { createdGroup ->
                                selectedGroup = createdGroup
                                navController.navigate(route = GroupDetails.getScreenName()) {
                                    popUpTo(CreateGroup.getScreenName()) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        showToast("Cannot get current user. Please retry later!!!")
                    }
                }
                composable(
                    route = ExploreGroup.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    if (homeViewModel.currentUser != null) {
                        ExploreGroup.ExploreGroupScreen(
                            homeViewModel.currentUser!!,
                            paddingValues,
                            localImageLoaderValue,
                            exploreGroupViewModel,
                            searchViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToGroupDetails = { group ->
                                selectedGroup = group
                                navController.navigate(route = GroupDetails.getScreenName())
                            }
                        )
                    }
                }
                composable(
                    route = GroupDetails.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    val picker = rememberPlatformImagePicker(
                        context = context,
                        onImagePicked = { uri -> userInformationViewModel.updateCover(uri) },
                        onVideoPicked = {}
                    )
                    if (selectedGroup != null) {
                        GroupDetails.GroupDetailsScreen(
                            homeViewModel.currentUser!!,
                            picker,
                            selectedGroup!!.id,
                            paddingValues,
                            localImageLoaderValue,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.background),
                            homeViewModel,
                            searchViewModel,
                            loadingViewModel,
                            groupDetailsViewModel,
                            onNavigateToShowImageScreen = { image ->
                                selectedImage = image
                                navController.navigate(route = ShowImage.getScreenName())
                            },
                            onNavigateToUserInformation = { user ->
                                selectedUser = user
                                navController.navigate(route = UserInformation.getScreenName())
                            },
                            onNavigateBack = {
                                if (homeViewModel.currentUser != null && homeViewModel.currentUser!!.groups.isNotEmpty()) {
                                    val selectGroupRoute = SelectGroup.getScreenName()
                                    val canPopToSelectGroup = try {
                                        navController.getBackStackEntry(selectGroupRoute)
                                        true
                                    } catch (e: IllegalArgumentException) {
                                        false
                                    }
                                    if (canPopToSelectGroup) {
                                        navController.popBackStack(
                                            route = selectGroupRoute,
                                            inclusive = false
                                        )
                                    } else {
                                        navController.popBackStack()
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                                coroutineScope.launch {
                                    //Delay a little bit to wait for animation
                                    delay(200)
                                    //Reset old group info
                                    groupDetailsViewModel.resetFetchGroupInfoState()
                                }
                            },
                            onNavigateToUploadNewsfeed = { new ->
                                updateNew = new
                                uploadNewsfeedViewModel.updateGroupId(selectedGroup!!.id)
                                if (groupDetailsViewModel.fetchGroupInfoState.value != null) {
                                    uploadNewsfeedViewModel.getGroupMembersFromGroupDetails(
                                        groupDetailsViewModel.fetchGroupInfoState.value!!.members
                                    )
                                }
                                navController.navigate(route = UploadNewsfeed.getScreenName())
                            },
                            onNavigateToCommentScreen = { new ->
                                selectedNew = new
                                navController.navigate(route = Comment.getScreenName())
                            },
                            onClickInviteButton = {
                                navController.navigate(route = InviteMember.getScreenName())
                            },
                            onLeaveGroup = {
                                navController.popBackStack()
                                if (homeViewModel.currentUser != null && selectedGroup != null) {
                                    //Remove group in current user group list
                                    homeViewModel.currentUser!!.groups.remove(selectedGroup!!.id)
                                }
                                coroutineScope.launch {
                                    //Delay a little bit to wait for animation
                                    delay(200)
                                    //Reset old group info
                                    groupDetailsViewModel.resetFetchGroupInfoState()
                                }
                            },
                            onManageMembers = { group ->
                                selectedGroup = group
                                navController.navigate(route = ManageMembers.getScreenName())
                            }
                        )
                    } else {
                        showToast("Cannot get group info this time. Please retry later!")
                    }
                }
                composable(
                    route = SelectGroup.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    SelectGroup.SelectGroupScreen(
                        homeViewModel.currentUser!!,
                        selectGroupViewModel,
                        searchViewModel,
                        paddingValues,
                        localImageLoaderValue,
                        onNavigateBack = {
                            searchViewModel.updateQuery("")
                            navController.popBackStack()
                        },
                        onNavigateToCreateGroup = {
                            navController.navigate(CreateGroup.getScreenName())
                        },
                        onNavigateToSelectedGroup = { group ->
                            selectedGroup = group
                            navController.navigate(route = GroupDetails.getScreenName())
                        }
                    )
                }
                composable(
                    route = InviteMember.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    if (selectedGroup != null) {
                        InviteMember.InviteMemberScreen(
                            selectedGroup!!,
                            homeViewModel.currentUser!!,
                            paddingValues,
                            localImageLoaderValue,
                            inviteMemberViewModel,
                            searchViewModel,
                            onNavigateBack = {
                                searchViewModel.updateQuery("")
                                navController.popBackStack()
                            }
                        )
                    } else {
                        showToast("Cannot get group info to share this time. Please retry later!")
                    }
                }
                composable(
                    route = "groups/{groupId}"
                ) { backStackEntry ->
                    val picker = rememberPlatformImagePicker(
                        context = context,
                        onImagePicked = { uri -> createGroupViewModel.updateAvatar(uri) },
                        onVideoPicked = {}
                    )

                    val groupId = backStackEntry.arguments?.getString("groupId")!!

                    GroupDetails.GroupDetailsScreen(
                        currentUser = homeViewModel.currentUser!!,
                        imagePicker = picker,
                        groupId = groupId,
                        paddingValues = paddingValues,
                        localImageLoaderValue = localImageLoaderValue,
                        homeViewModel = homeViewModel,
                        searchViewModel = searchViewModel,
                        loadingViewModel = loadingViewModel,
                        groupDetailsViewModel = groupDetailsViewModel,
                        onNavigateToShowImageScreen = { image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        },
                        onNavigateToUserInformation = { user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateBack = {
                            if (homeViewModel.currentUser != null && homeViewModel.currentUser!!.groups.isNotEmpty()) {
                                val selectGroupRoute = SelectGroup.getScreenName()
                                val canPopToSelectGroup = try {
                                    navController.getBackStackEntry(selectGroupRoute)
                                    true
                                } catch (e: IllegalArgumentException) {
                                    false
                                }
                                if (canPopToSelectGroup) {
                                    navController.popBackStack(
                                        route = selectGroupRoute,
                                        inclusive = false
                                    )
                                } else {
                                    navController.popBackStack()
                                }
                            } else {
                                navController.popBackStack()
                            }
                            coroutineScope.launch {
                                //Delay a little bit to wait for animation
                                delay(200)
                                //Reset old group info
                                groupDetailsViewModel.resetFetchGroupInfoState()
                            }
                        },
                        onNavigateToUploadNewsfeed = { new ->
                            updateNew = new
                            uploadNewsfeedViewModel.updateGroupId(selectedGroup!!.id)
                            if (groupDetailsViewModel.fetchGroupInfoState.value != null) {
                                uploadNewsfeedViewModel.getGroupMembersFromGroupDetails(
                                    groupDetailsViewModel.fetchGroupInfoState.value!!.members
                                )
                            }
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        },
                        onNavigateToCommentScreen = { new ->
                            selectedNew = new
                            navController.navigate(route = Comment.getScreenName())
                        },
                        onClickInviteButton = {
                            navController.navigate(route = InviteMember.getScreenName())
                        },
                        onLeaveGroup = {
                            navController.popBackStack()
                            if (homeViewModel.currentUser != null && selectedGroup != null) {
                                //Remove group in current user group list
                                homeViewModel.currentUser!!.groups.remove(selectedGroup!!.id)
                            }
                            coroutineScope.launch {
                                //Delay a little bit to wait for animation
                                delay(200)
                                //Reset old group info
                                groupDetailsViewModel.resetFetchGroupInfoState()
                            }
                        },
                        onManageMembers = { group ->
                            selectedGroup = group
                            navController.navigate(route = ManageMembers.getScreenName())
                        }
                    )
                }
                composable(
                    route = ManageMembers.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    if (selectedGroup != null && homeViewModel.currentUser != null) {
                        ManageMembers.ManageMembersScreen(
                            homeViewModel.currentUser!!,
                            selectedGroup,
                            manageMembersViewModel,
                            searchViewModel,
                            loadingViewModel,
                            paddingValues,
                            localImageLoaderValue,
                            onNavigateBack = {
                                searchViewModel.updateQuery("")
                                navController.popBackStack()
                            },
                            onInviteMembers = {
                                navController.navigate(route = InviteMember.getScreenName())
                            },
                            onNavigateToUserInformationScreen = { user ->
                                selectedUser = user
                                navController.navigate(route = UserInformation.getScreenName())
                            },
                            onNavigateToSelectGroupScreen = {
                                //Remove group from current user group list
                                homeViewModel.currentUser!!.groups.remove(selectedGroup.id)
                                navController.navigate(route = SelectGroup.getScreenName()) {
                                    popUpTo(SelectGroup.getScreenName()) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        showToast("Cannot open manage members screen now. Please try again!!!")
                        navController.popBackStack()
                    }
                }

                composable(
                    route = Privacy.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    Privacy.PrivacyScreen(
                        onClickBack = {
                            navController.popBackStack()
                        },
                        onUnderstandClicked = {
                            homeViewModel.currentUser?.let { user ->
                                loginHistoryViewModel.acknowledgePrivacyRead(user)
                            }
                        }
                    )
                }

                composable(
                    route = SecuritySettings.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    if (homeViewModel.currentUser != null) {
                        SecuritySettings.SecuritySettingsScreen(
                            homeViewModel.currentUser!!,
                            paddingValues,
                            securitySettingsViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onChangePassword = {
                                navController.navigate(ChangePassword.getScreenName())
                            },
                            onNavigateTo2FAScreen = {
                                navController.navigate(TwoFA.getScreenName())
                            },
                            onLoginActivity = {
                                navController.navigate(LoginHistory.getScreenName())
                            }
                        )
                    } else {
                        showToast("Cannot get your information now. Please try again!")
                    }
                }

                composable(
                    route = NotificationConfigs.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    NotificationConfigs.NotificationConfigsScreen(
                        paddingValues,
                        notificationConfigsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = ChangePassword.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    ChangePassword.ChangePasswordScreen(
                        paddingValues,
                        currentUser = if (homeViewModel.currentUser != null) homeViewModel.currentUser!! else UserInstance(),
                        changePasswordViewModel = changePasswordViewModel,
                        loadingViewModel = loadingViewModel,
                        onNavigateToForgotPasswordScreen = {
                            navController.navigate(ForgotPassword.getScreenName())
                        },
                        onNavigateToSignInScreen = {
                            navController.navigate(SignIn.getScreenName()) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = TwoFA.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    TwoFA.TwoFAScreen(
                        paddingValues,
                        if (homeViewModel.currentUser != null) homeViewModel.currentUser!! else UserInstance(),
                        twoFAViewModel,
                        onContinue = { secret ->
                            localSecret = secret
                            navController.navigate(VerifyOTP.getScreenName())
                        },
                        onNavigateBack = {
                            localSecret = ""
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = VerifyOTP.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    VerifyOTP.VerifyOTPScreen(
                        paddingValues,
                        localImageLoaderValue,
                        if (homeViewModel.currentUser != null) homeViewModel.currentUser!! else routerViewModel.currentUser.value,
                        secretKey = localSecret,
                        loadingViewModel,
                        verifyOTPViewModel,
                        onNavigateToVerifyOTPSuccessScreen = { code ->
                            backupCode = code
                            isEnable2FAFlow = true
                            navController.navigate(TwoFactorEnabled.getScreenName())
                        },
                        onNavigateToHomeScreen = {
                            navController.navigate(route = Home.getScreenName())
                        },
                        onNavigateToBackupCodeScreen = {
                            navController.navigate(route = BackUpCode.getScreenName())
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToSignInScreen = {
                            navController.navigate(SignIn.getScreenName()) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = TwoFactorEnabled.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    TwoFactorEnabled.TwoFactorEnabledScreen(
                        paddingValues,
                        backupCode,
                        isEnable2FAFlow,
                        twoFactorEnabledViewModel,
                        onReturnClick = {
                            if (isEnable2FAFlow) {
                                navController.popBackStack(SecuritySettings.getScreenName(), false)
                            } else {
                                navController.navigate(Home.getScreenName()) {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable(
                    route = BackUpCode.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    BackUpCode.BackupCodeScreen(
                        paddingValues,
                        loadingViewModel,
                        backUpCodeViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToVerifyBackupCodeSuccessScreen = { newBackupCode ->
                            backupCode = newBackupCode
                            isEnable2FAFlow = false
                            navController.navigate(route = TwoFactorEnabled.getScreenName())
                        }
                    )
                }

                composable(
                    route = LoginHistory.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    LoginHistory.LoginHistoryScreen(
                        currentUser = homeViewModel.currentUser ?: UserInstance(),
                        loginHistoryViewModel,
                        modifier = Modifier
                            .padding(paddingValues),
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = PersonalInformation.getScreenName(),
                    enterTransition = DefaultNavAnimations.enter,
                    popEnterTransition = DefaultNavAnimations.popEnter,
                    exitTransition = DefaultNavAnimations.exit,
                    popExitTransition = DefaultNavAnimations.popExit
                ) {
                    val picker = rememberPlatformImagePicker(
                        context = context,
                        onImagePicked = { uri -> personalInformationViewModel.onAvatarPicked(uri) },
                        onVideoPicked = {}
                    )
                    if(selectedUser != null) {
                        PersonalInformation.PersonalInformationScreen(
                            selectedUser!!,
                            imagePicker = picker,
                            paddingValues,
                            personalInformationViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        showToast("Cannot get user information now. Please try again!!!")
                        navController.popBackStack()
                    }
                }
            }
        }
        if (isSyncLoading) {
            GifLoading.GifLoadingScreen(
                localImageLoaderValue,
                "loading_gif",
                "Syncing your data..."
            )
        }
    }
}