package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.data.model.call.SharedCallData
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
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
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.BottomNavigationBar

@Composable
fun SetUpNavigation(context: Any, platformContext : PlatformContext) {
    val navController = rememberNavController()
    val navigationHandler = com.minhtu.firesocialmedia.platform.rememberNavigationHandler(navController)
    val startDestination = SignIn.getScreenName()
    var selectedImage = ""
    var selectedUser: UserInstance? = null
    lateinit var selectedNew : NewsInstance

    // Shared viewModels
    val signUpViewModel: SignUpViewModel = platformViewModel { SignUpViewModel() }
    val signInViewModel: SignInViewModel = platformViewModel { SignInViewModel() }
    val loadingViewModel: LoadingViewModel = platformViewModel { LoadingViewModel() }
    val forgotPasswordViewModel: ForgotPasswordViewModel = platformViewModel { ForgotPasswordViewModel() }
    val commentViewModel : CommentViewModel = platformViewModel { CommentViewModel() }
    val searchViewModel : SearchViewModel = platformViewModel { SearchViewModel() }
    val uploadNewsfeedViewModel : UploadNewfeedViewModel = platformViewModel { UploadNewfeedViewModel() }
    val informationViewModel : InformationViewModel = platformViewModel { InformationViewModel() }
    val friendViewModel : FriendViewModel = platformViewModel { FriendViewModel() }
    val userInformationViewModel : UserInformationViewModel = platformViewModel { UserInformationViewModel() }
    val showImageViewModel : ShowImageViewModel = platformViewModel { ShowImageViewModel() }
    val notificationViewModel : NotificationViewModel = platformViewModel { NotificationViewModel() }
    val homeViewModel: HomeViewModel = platformViewModel { HomeViewModel() }

    var updateNew : NewsInstance? = null
    var relatedNew : NewsInstance? = null
    var callee : UserInstance? = null
    var caller : UserInstance? = null
    var sessionId = ""
    var remoteOffer : OfferAnswer? = null
    var remoteVideoOffer : OfferAnswer? = null
    val callingViewModel : CallingViewModel = platformViewModel { CallingViewModel() }
    val videoCallViewModel : VideoCallViewModel = platformViewModel { VideoCallViewModel() }

    // Platform-specific helpers
    setupSignInLauncher(context, signInViewModel, platformContext)

    val listScreenNeedBottomBar = listOf(Screen.Home.route, Screen.Friend.route, Screen.Notification.route, Screen.Settings.route)

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
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = startDestination){
            composable(
                route = SignIn.getScreenName(),
                enterTransition = DefaultNavAnimations.enter,
                popEnterTransition = DefaultNavAnimations.popEnter,
                exitTransition = DefaultNavAnimations.exit,
                popExitTransition = DefaultNavAnimations.popExit){
                SignIn.SignInScreen(
                    platformContext,
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
                route = SignUp.getScreenName(),
                enterTransition = DefaultNavAnimations.enter,
                popEnterTransition = DefaultNavAnimations.popEnter,
                exitTransition = DefaultNavAnimations.exit,
                popExitTransition = DefaultNavAnimations.popExit){
                SignUp.SignUpScreen(
                    platformContext,
                    signUpViewModel,
                    loadingViewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF132026)),
                    onNavigateToSignInScreen = { navController.navigate(route = SignIn.getScreenName()) },
                    onNavigateToInformationScreen = { navController.navigate(route = Information.getScreenName()) }
                )
            }
            composable(
                route = Information.getScreenName(),
                enterTransition = DefaultNavAnimations.enter,
                popEnterTransition = DefaultNavAnimations.popEnter,
                exitTransition = DefaultNavAnimations.exit,
                popExitTransition = DefaultNavAnimations.popExit){
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
                    platformContext,
                    homeViewModel,
                    loadingViewModel,
                    SharedCallData.navigateToCallingScreenFromNotification,
                    paddingValues = paddingValues,
                    onNavigateToUploadNews = { new ->
                        updateNew = new
                        navController.navigate(route = UploadNewsfeed.getScreenName())
                    },
                    onNavigateToShowImageScreen = { image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())
                    },
                    onNavigateToSearch = { navController.navigate(route = Search.getScreenName()) },
                    onNavigateToSignIn = { navController.navigate(route = SignIn.getScreenName()) },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = UserInformation.getScreenName())
                    },
                    onNavigateToCommentScreen = { new ->
                        selectedNew = new
                        navController.navigate(route = Comment.getScreenName())
                    },
                    onNavigateToCallingScreen = { sessionID, callerId, calleeId, offer ->
                        sessionId = sessionID
                        remoteOffer = offer
                        caller = if(callerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else platformContext.database.getUser(callerId)
                        callee = if(calleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else platformContext.database.getUser(calleeId)
                        navController.navigate(route = Calling.getScreenName())
                    },
                    onNavigateToCallingScreenWithUI = {
                        sessionId = SharedCallData.sessionId
                        caller = if(SharedCallData.callerId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else platformContext.database.getUser(SharedCallData.callerId)
                        callee = if(SharedCallData.calleeId == homeViewModel.currentUser?.uid) homeViewModel.currentUser else platformContext.database.getUser(SharedCallData.calleeId)
                        navController.navigate(route = Calling.getScreenName())
                    },
                    navigationHandler
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
                    platform = platformContext,
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
                    platformContext,
                    selectedImage,
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
                Search.SearchScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                    platformContext,
                    searchViewModel,
                    homeViewModel,
                    navigationHandler,
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
                    platform = platformContext,
                    imagePicker = picker,
                    user = selectedUser,
                    isCurrentUser = selectedUser == homeViewModel.currentUser,
                    paddingValues = paddingValues,
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
                    navController = navigationHandler,
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
                ) {
                    navController.navigate(route = Home.getScreenName())
                }
            }
            composable(
                route = ForgotPassword.getScreenName(),
                enterTransition = DefaultNavAnimations.enter,
                popEnterTransition = DefaultNavAnimations.popEnter,
                exitTransition = DefaultNavAnimations.exit,
                popExitTransition = DefaultNavAnimations.popExit) {
                ForgotPassword.ForgotPasswordScreen(
                    platformContext,
                    forgotPasswordViewModel,
                    loadingViewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF132026)),
                    onNavigateToSignInScreen = { navController.navigate(route = SignIn.getScreenName()) }
                )
            }
            composable(
                route = Screen.Friend.route,
                enterTransition = DefaultNavAnimations.enter,
                popEnterTransition = DefaultNavAnimations.popEnter,
                exitTransition = DefaultNavAnimations.exit,
                popExitTransition = DefaultNavAnimations.popExit){
                Friend.FriendScreen(
                    platformContext,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    paddingValues = paddingValues,
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
                    platformContext,
                    paddingValues = paddingValues,
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
                    platformContext,
                    paddingValues = paddingValues,
                    homeViewModel = homeViewModel,
                    onNavigateToSignIn = {
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
                    relatedNew!!,
                    onNavigateToShowImageScreen = { image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())
                    },
                    onNavigateToUserInformation = { user ->
                        selectedUser = user
                        navController.navigate(route = UserInformation.getScreenName())
                    },
                    onNavigateToHomeScreen = { navController.navigate(route = Home.getScreenName()) },
                    homeViewModel,
                    commentViewModel,
                    navigationHandler
                )
            }
            composable(
                route = Calling.getScreenName(),
                enterTransition = DefaultNavAnimations.enter,
                popEnterTransition = DefaultNavAnimations.popEnter,
                exitTransition = DefaultNavAnimations.exit,
                popExitTransition = DefaultNavAnimations.popExit) {
                homeViewModel.updateIsInCall(true)
                Calling.CallingScreen(
                    platformContext,
                    sessionId,
                    callee,
                    caller,
                    homeViewModel.currentUser,
                    remoteOffer,
                    SharedCallData.navigateToCallingScreenFromNotification,
                    callingViewModel,
                    navigationHandler,
                    onStopCall = { homeViewModel.updateIsInCall(false) },
                    onNavigateToVideoCall = { sessionID, videoOffer ->
                        sessionId = sessionID
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
                    platformContext,
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