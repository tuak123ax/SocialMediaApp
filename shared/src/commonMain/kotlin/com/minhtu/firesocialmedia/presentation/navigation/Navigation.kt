package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.minhtu.firesocialmedia.di.AppModule
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.di.ViewModelProvider
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SharedCallData
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.platformViewModel
import com.minhtu.firesocialmedia.platform.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.platform.setupSignInLauncher
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
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformation
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
fun SetUpNavigation(context: Any, platformContext : PlatformContext) {
    val navController = rememberNavController()
    val navigationHandler = com.minhtu.firesocialmedia.platform.rememberNavigationHandler(navController)
    var selectedImage = ""
    var selectedUser: UserInstance? = null
    lateinit var selectedNew : NewsInstance
    val coroutineScope = rememberCoroutineScope()
    val localImageLoaderValue = LocalImageLoader provides remember { generateImageLoader() }

    // Shared viewModels
    val signUpViewModel: SignUpViewModel = platformViewModel { ViewModelProvider.createSignUpViewModel(platformContext) }
    val signInViewModel: SignInViewModel = platformViewModel { ViewModelProvider.createSignInViewModel(platformContext) }
    val loadingViewModel: LoadingViewModel = platformViewModel { ViewModelProvider.createLoadingViewModel() }
    val forgotPasswordViewModel: ForgotPasswordViewModel = platformViewModel { ViewModelProvider.createForgotPasswordViewModel(platformContext) }
    val commentViewModel : CommentViewModel = platformViewModel { ViewModelProvider.createCommentViewModel(platformContext) }
    val searchViewModel : SearchViewModel = platformViewModel { ViewModelProvider.createSearchViewModel() }
    val uploadNewsfeedViewModel : UploadNewfeedViewModel = platformViewModel { ViewModelProvider.createUploadNewfeedViewModel(platformContext) }
    val informationViewModel : InformationViewModel = platformViewModel { ViewModelProvider.createInformationViewModel(platformContext) }
    val friendViewModel : FriendViewModel = platformViewModel { ViewModelProvider.createFriendViewModel(platformContext) }
    val userInformationViewModel : UserInformationViewModel = platformViewModel { ViewModelProvider.createUserInformationViewModel(platformContext) }
    val showImageViewModel : ShowImageViewModel = platformViewModel { ViewModelProvider.createShowImageViewModel(platformContext) }
    val notificationViewModel : NotificationViewModel = platformViewModel { ViewModelProvider.createNotificationViewModel(platformContext) }
    val homeViewModel: HomeViewModel = platformViewModel { ViewModelProvider.createHomeViewModel(platformContext) }
    val syncDataUseCase = AppModule.provideSyncDataUseCase(AppModule.provideCommonDbRepository(platformContext))

    var updateNew : NewsInstance? = null
    lateinit var relatedNew : NewsInstance
    var callee : UserInstance? = null
    var caller : UserInstance? = null
    var sessionId = ""
    var remoteOffer : OfferAnswer? = null
    var remoteVideoOffer : OfferAnswer? = null
    val callingViewModel : CallingViewModel = platformViewModel { ViewModelProvider.createCallingViewModel(platformContext) }
    val videoCallViewModel : VideoCallViewModel = platformViewModel { ViewModelProvider.createVideoCallViewModel(platformContext) }

    val isSyncLoading by loadingViewModel.syncLoading.collectAsState()

    // Platform-specific helpers
    setupSignInLauncher(context, signInViewModel, platformContext)

    val snackBarHostState = remember { SnackbarHostState() }
    val networkStatus by platformContext.networkMonitor.isOnline.collectAsStateWithLifecycle(initialValue = null)

    // Track previous value
    var wasOffline by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(networkStatus) {
        if(networkStatus != null) {
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
                    if(homeViewModel.currentUser != null) {
                        //Show loading while sync data
                        loadingViewModel.showSyncLoading()
                        //Sync data
                        val syncDataResult = syncDataUseCase.invoke(homeViewModel.currentUser!!.uid)
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

    val listScreenNeedBottomBar = listOf(Screen.Home.route, Screen.Friend.route, Screen.Notification.route, Screen.Settings.route)

    Box(modifier = Modifier.Companion
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)){
        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
                if(currentDestination in listScreenNeedBottomBar) {
                    BottomNavigationBar(
                        currentRoute = currentDestination,
                        onNavigate = { route -> navController.navigate(route) },
                        homeViewModel = homeViewModel,
                        onNavigateToUploadNews = {
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color.White)
                    )
                }
            },
            snackbarHost = { UiUtils.MySnackBarHost(snackBarHostState, networkStatus) }
        ) { paddingValues ->
            val startDestination by produceState<String?>(initialValue = null) {
                value = if (platformContext.crypto.loadAccount() == null) SignIn.getScreenName() else Home.getScreenName()
            }
            if(startDestination != null) {
                NavHost(navController = navController, startDestination = startDestination!!){
                    composable(
                        route = SignIn.getScreenName()){
                        SignIn.SignInScreen(
                            signInViewModel,
                            loadingViewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF132026)),
                            onNavigateToSignUpScreen = { navController.navigate(route = SignUp.getScreenName()) },
                            onNavigateToHomeScreen = { navController.navigate(route = Home.getScreenName()) },
                            onNavigateToInformationScreen = { navController.navigate(route = Information.getScreenName()) },
                            onNavigateToForgotPasswordScreen = { navController.navigate(route = ForgotPassword.getScreenName()) }
                        )
                    }
                    composable(
                        route = SignUp.getScreenName()){
                        SignUp.SignUpScreen(
                            signUpViewModel,
                            loadingViewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF132026)),
                            onNavigateToSignInScreen = {
                                navController.popBackStack() },
                            onNavigateToInformationScreen = { navController.navigate(route = Information.getScreenName()) }
                        )
                    }
                    composable(
                        route = Information.getScreenName()){
                        val picker = rememberPlatformImagePicker(
                            context = context,
                            onImagePicked = { uri -> informationViewModel.updateAvatar(uri) },
                            onVideoPicked = {}
                        )
                        Information.InformationScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF132026)),
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
                        popExitTransition = DefaultNavAnimations.popExit){
                        Home.HomeScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
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
                                navController.navigate(route = SignIn.getScreenName()) },
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
                                caller = if(callingRequestData.callerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(callingRequestData.callerId)
                                callee = if(callingRequestData.calleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(callingRequestData.calleeId)
                                navController.navigate(route = Calling.getScreenName())
                            },
                            onNavigateToCallingScreenWithUI = {
                                sessionId = SharedCallData.sessionId
                                caller = if(SharedCallData.callerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(SharedCallData.callerId)
                                callee = if(SharedCallData.calleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else homeViewModel.findUserById(SharedCallData.calleeId)
                                navController.navigate(route = Calling.getScreenName())
                            }
                        )
                    }
                    composable(
                        route = UploadNewsfeed.getScreenName(),
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit){
                        val picker = rememberPlatformImagePicker(
                            context = context,
                            onImagePicked = { uri -> uploadNewsfeedViewModel.updateImage(uri) },
                            onVideoPicked = { uri -> uploadNewsfeedViewModel.updateVideo(uri) }
                        )
                        UploadNewsfeed.UploadNewsfeedScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            imagePicker = picker,
                            homeViewModel = homeViewModel,
                            uploadNewsfeedViewModel = uploadNewsfeedViewModel,
                            loadingViewModel = loadingViewModel,
                            updateNew = updateNew,
                            onNavigateToHomeScreen = { navController.navigate(route = Home.getScreenName()) }
                        )
                    }
                    composable(
                        route = ShowImage.getScreenName(),
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit) {
                        ShowImage.ShowImageScreen(
                            selectedImage,
                            localImageLoaderValue = localImageLoaderValue,
                            showImageViewModel = showImageViewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.Black),
                            onNavigateToHomeScreen = { navController.navigate(route = Home.getScreenName()) }
                        )
                    }
                    composable(
                        route = Search.getScreenName(),
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit) {
                        coroutineScope.launch(Dispatchers.IO) {
                            delay(700)
                            //Reset search text
                            searchViewModel.updateQuery("")
                        }
                        Search.SearchScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.White),
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
                        popExitTransition = DefaultNavAnimations.popExit){
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
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.White),
                            homeViewModel = homeViewModel,
                            friendViewModel = friendViewModel,
                            userInformationViewModel = userInformationViewModel,
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
                            onNavigateToUploadNewsfeed = { new ->
                                updateNew = new
                                navController.navigate(route = UploadNewsfeed.getScreenName())
                            },
                            onNavigateToCallingScreen = { user ->
                                if(user != null) {
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
                        popExitTransition = DefaultNavAnimations.popExit) {
                        Comment.CommentScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.White),
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
                        route = ForgotPassword.getScreenName()) {
                        ForgotPassword.ForgotPasswordScreen(
                            forgotPasswordViewModel,
                            loadingViewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF132026)),
                            onNavigateToSignInScreen = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = Screen.Friend.route,
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit){
                        coroutineScope.launch(Dispatchers.IO) {
                            delay(700)
                            //Reset search text
                            searchViewModel.updateQuery("")
                        }
                        Friend.FriendScreen(
                            platformContext,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
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
                        popExitTransition = DefaultNavAnimations.popExit){
                        Notification.NotificationScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
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
                            }
                        )
                    }
                    composable(
                        route = Screen.Settings.route,
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit){
                        Settings.SettingsScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            paddingValues = paddingValues,
                            homeViewModel = homeViewModel,
                            onNavigateToSignIn = {
                                //Clear email/password before navigate
                                signInViewModel.reset()
                                navController.navigate(route = SignIn.getScreenName())
                            }
                        )
                    }
                    composable(
                        route = PostInformation.getScreenName(),
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit) {
                        PostInformation.PostInformationScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
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
                            onNavigateToHomeScreen = { numberOfComments ->
                                homeViewModel.addCommentCountData(relatedNew.id, numberOfComments)
                                navController.navigate(route = Home.getScreenName()) },
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            homeViewModel,
                            commentViewModel
                        )
                    }
                    composable(
                        route = Calling.getScreenName(),
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit) {
                        Calling.CallingScreen(
                            localImageLoaderValue = localImageLoaderValue,
                            sessionId,
                            callee,
                            caller,
                            homeViewModel.currentUser,
                            remoteOffer,
                            SharedCallData.navigateToCallingScreenFromNotification,
                            callingViewModel,
                            homeViewModel,
                            navigationHandler,
                            onStopCallAndNavigateBack = {
                                if(navigationHandler.getCurrentRoute() != Home.getScreenName()) {
                                    navigationHandler.navigateBack()
                                }
                                homeViewModel.resetCallEvent() },
                            onNavigateToVideoCall = { sessionID, videoOffer ->
                                sessionId = sessionID
                                remoteVideoOffer = videoOffer
                                remoteVideoOffer = videoOffer
                                navController.navigate(route = VideoCall.getScreenName())
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
                    composable(
                        route = VideoCall.getScreenName(),
                        enterTransition = DefaultNavAnimations.enter,
                        popEnterTransition = DefaultNavAnimations.popEnter,
                        exitTransition = DefaultNavAnimations.exit,
                        popExitTransition = DefaultNavAnimations.popExit) {
                        VideoCall.VideoCallScreen(
                            sessionId,
                            caller,
                            callee,
                            homeViewModel.currentUser?.uid,
                            remoteVideoOffer,
                            videoCallViewModel,
                            loadingViewModel,
                            navigationHandler,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
                }
            }
        }
        if (isSyncLoading) {
            GifLoading.GifLoadingScreen(localImageLoaderValue,
                "Syncing your data...")
        }
    }
}