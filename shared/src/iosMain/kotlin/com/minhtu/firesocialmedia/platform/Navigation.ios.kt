package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.comment.Comment
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.presentation.home.Home
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.information.Information
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
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
actual fun SetUpNavigation(context: Any, platformContext : PlatformContext) {
    var selectedImage = ""
    var selectedUser: UserInstance? = null
    lateinit var selectedNew : NewsInstance

    //Define shared viewModel instance to use for signUp and information screens.
    val signUpViewModel = remember { SignUpViewModel() }
    val signInViewModel = remember { SignInViewModel() }
    val loadingViewModel = remember { LoadingViewModel() }
    val forgotPasswordViewModel = remember { ForgotPasswordViewModel() }
    val commentViewModel = remember { CommentViewModel() }
    val searchViewModel = remember { SearchViewModel() }
    val uploadNewsfeedViewModel = remember { UploadNewfeedViewModel() }
    val informationViewModel = remember { InformationViewModel() }
    val friendViewModel = remember { FriendViewModel() }
    val userInformationViewModel = remember { UserInformationViewModel() }
    val showImageViewModel = remember { ShowImageViewModel() }
    val notificationViewModel = remember { NotificationViewModel() }
    //Define shared viewModel instance to use for Home and Search screens.
    val homeViewModel = remember { HomeViewModel() }
    //Shared instance used for uploadNewFeeds screen.
    var updateNew : NewsInstance? = null
    //Shared instance used for Notification and PostInformation screen.
    var relatedNew : NewsInstance? = null
    val listScreenNeedBottomBar = listOf(
        IosScreen.HomeScreen,
        IosScreen.FriendScreen,
        IosScreen.NotificationScreen,
        IosScreen.SettingsScreen)
    val currentScreen = remember { mutableStateOf<IosScreen>(IosScreen.SignInScreen) }
    val iosNavigationHandler = IOSNavigationHandlerWithStack(currentScreen)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            //Bottom navigation bar
            val currentDestination = currentScreen.value
            if(currentDestination in listScreenNeedBottomBar) {
                BottomNavigationBar(iosNavigationHandler,
                    homeViewModel,
                    onNavigateToUploadNews = {
                    iosNavigationHandler.navigateTo(IosScreen.UploadNewsScreen.toString())
                },
                    Modifier
                        .fillMaxWidth(),
                    useDefaultInsets = false,
                    useCustomBar = true
                )
            }
        }
    ){
        paddingValues ->
        logMessage("Navigation") { currentScreen.value.toString() }
        when (currentScreen.value) {
            IosScreen.CommentScreen -> Comment.CommentScreen(modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White),
                platformContext,
                showCloseIcon = true,
                commentViewModel,
                currentUser = homeViewModel.currentUser!!,
                selectedNew = selectedNew,
                onNavigateToShowImageScreen = {image ->
                    selectedImage = image
                    iosNavigationHandler.navigateTo(IosScreen.ShowImageScreen.toString()) },
                onNavigateToUserInformation = {user ->
                    selectedUser = user
                    iosNavigationHandler.navigateTo(IosScreen.UserInformationScreen.toString())
                }) {
                iosNavigationHandler.navigateTo(IosScreen.HomeScreen.toString())
            }
            IosScreen.ForgotPasswordScreen -> ForgotPassword.ForgotPasswordScreen(
                platformContext,
                forgotPasswordViewModel,
                loadingViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF132026))
//                        .paint(
//                            painter = painterResource(id = R.drawable.background),
//                            contentScale = ContentScale.FillBounds)
                ,
                onNavigateToSignInScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.SignInScreen.toString())
                })
            IosScreen.FriendScreen -> Friend.FriendScreen(
                platformContext,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                paddingValues = paddingValues,
                searchViewModel,
                homeViewModel = homeViewModel,
                friendViewModel,
                onNavigateToUserInformation = {
                        user ->
                    selectedUser = user
                    iosNavigationHandler.navigateTo(IosScreen.UserInformationScreen.toString())
                },
                onNavigateToShowImageScreen = {
                        image ->
                    selectedImage = image
                    iosNavigationHandler.navigateTo(IosScreen.ShowImageScreen.toString())
                })
            IosScreen.HomeScreen -> Home.HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
//                        .paint(
//                            painter = painterResource(id = R.drawable.background),
//                            contentScale = ContentScale.FillBounds)
                ,
                platformContext,
                homeViewModel,
                loadingViewModel,
                navigateToCallingScreen = false, // iOS implementation will be added later
                paddingValues = paddingValues,
                onNavigateToUploadNews = {new ->
                    updateNew = new
                    iosNavigationHandler.navigateTo(IosScreen.UploadNewsScreen.toString())
                },
                onNavigateToShowImageScreen = {image ->
                    selectedImage = image
                    iosNavigationHandler.navigateTo(IosScreen.ShowImageScreen.toString()) },
                onNavigateToSearch = {
                    iosNavigationHandler.navigateTo(IosScreen.SearchScreen.toString())
                },
                onNavigateToSignIn = {
                    iosNavigationHandler.navigateTo(IosScreen.SignInScreen.toString())
                },
                onNavigateToUserInformation = {user ->
                    selectedUser = user
                    iosNavigationHandler.navigateTo(IosScreen.UserInformationScreen.toString())
                },
                onNavigateToCommentScreen = { new ->
                    selectedNew = new
                    iosNavigationHandler.navigateTo(IosScreen.CommentScreen.toString())
                },
                onNavigateToCallingScreen = { sessionId, caller, callee, offer ->
                    // iOS calling implementation will be added later
                    // iosNavigationHandler.navigateTo(IosScreen.CallingScreen.toString())
                },
                onNavigateToCallingScreenWithUI = {
                    // iOS calling with UI implementation will be added later
                    // iosNavigationHandler.navigateTo(IosScreen.CallingScreen.toString())
                },
                navHandler = iosNavigationHandler
            )
            IosScreen.InformationScreen -> Information.InformationScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF132026))
                ,
                platformContext,
                remember {
                    val iosImagePicker = IosImagePicker(
                        onImagePicked = { imageUri ->
                            informationViewModel.updateAvatar(imageUri)
                        },
                        onVideoPicked = {}
                    )
                    iosImagePicker.imagePicker
                },
                signUpViewModel,
                informationViewModel,
                loadingViewModel,
                onNavigateToHomeScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.HomeScreen.toString())
                }
            )
            IosScreen.LoadingScreen -> {}
            // Temporarily removed calling screens to get basic build working
            // IosScreen.CallingScreen -> {
            //     // Placeholder for calling screen - will be implemented later
            //     Box(
            //         modifier = Modifier
            //             .fillMaxSize()
            //             .background(Color.White),
            //         contentAlignment = Alignment.Center
            //     ) {
            //         Text("Calling Screen - iOS implementation coming soon")
            //     }
            // }
            // IosScreen.VideoCallScreen -> {
            //     // Placeholder for video call screen - will be implemented later
            //     Box(
            //         modifier = Modifier
            //             .fillMaxSize()
            //             .background(Color.White),
            //         contentAlignment = Alignment.Center
            //     ) {
            //         Text("Video Call Screen - iOS implementation coming soon")
            //     }
            // }
            IosScreen.NotificationScreen -> Notification.NotificationScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                platformContext,
                paddingValues = paddingValues,
                searchViewModel = searchViewModel,
                homeViewModel = homeViewModel,
                notificationViewModel = notificationViewModel,
                loadingViewModel = loadingViewModel,
                onNavigateToPostInformation = {
                        new ->
                    relatedNew = new
                    iosNavigationHandler.navigateTo(IosScreen.PostInformationScreen.toString())
                },
                onNavigateToUserInformation = {
                        user ->
                    selectedUser = user
                    iosNavigationHandler.navigateTo(IosScreen.UserInformationScreen.toString())
                }
            )
            IosScreen.PostInformationScreen -> PostInformation.PostInformationScreen(
                platformContext,
                relatedNew!!,
                onNavigateToShowImageScreen = {
                        image ->
                    selectedImage = image
                    iosNavigationHandler.navigateTo(IosScreen.ShowImageScreen.toString())
                },
                onNavigateToUserInformation = {
                        user ->
                    selectedUser = user
                    iosNavigationHandler.navigateTo(IosScreen.UserInformationScreen.toString())
                },
                onNavigateToHomeScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.HomeScreen.toString())
                },
                homeViewModel,
                commentViewModel,
                iosNavigationHandler
            )
            IosScreen.SearchScreen -> Search.SearchScreen(modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White),
                platformContext,
                searchViewModel,
                homeViewModel,
                iosNavigationHandler,
                onNavigateToUserInformation = {user ->
                    selectedUser = user
                    iosNavigationHandler.navigateTo(IosScreen.UserInformationScreen.toString())
                },
                onNavigateToShowImageScreen = {image ->
                    selectedImage = image
                    iosNavigationHandler.navigateTo(IosScreen.ShowImageScreen.toString())
                },
                onNavigateToCommentScreen = { new ->
                    selectedNew = new
                    iosNavigationHandler.navigateTo(IosScreen.CommentScreen.toString())
                },
                onNavigateToUploadNewsFeed = { new ->
                    iosNavigationHandler.navigateTo(IosScreen.UploadNewsScreen.toString())
                }
            )
            IosScreen.SettingsScreen -> Settings.SettingsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                platformContext,
                paddingValues = paddingValues,
                homeViewModel = homeViewModel,
                onNavigateToSignIn = {
                    iosNavigationHandler.navigateTo(IosScreen.SignInScreen.toString())
                }
            )
            IosScreen.ShowImageScreen -> ShowImage.ShowImageScreen(
                platformContext,
                selectedImage,
                showImageViewModel = showImageViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black),
                onNavigateToHomeScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.HomeScreen.toString())
                }
            )
            IosScreen.SignInScreen -> SignIn.SignInScreen(
                platformContext,
                signInViewModel,
                loadingViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF132026))
                ,
                onNavigateToSignUpScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.SignUpScreen.toString())
                },
                onNavigateToHomeScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.HomeScreen.toString())
                },
                onNavigateToInformationScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.InformationScreen.toString())
                },
                onNavigateToForgotPasswordScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.ForgotPasswordScreen.toString())
                }
            )
            IosScreen.SignUpScreen -> SignUp.SignUpScreen(
                platformContext,
                signUpViewModel,
                loadingViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF132026))
//                            .paint(
//                                painter = painterResource(id = R.drawable.background),
//                                contentScale = ContentScale.FillBounds)
                ,
                onNavigateToSignInScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.SignInScreen.toString())
                },
                onNavigateToInformationScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.InformationScreen.toString())
                })
            IosScreen.UploadNewsScreen -> UploadNewsfeed.UploadNewsfeedScreen(modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
//                        .paint(
//                            painter = painterResource(id = R.drawable.background),
//                            contentScale = ContentScale.FillBounds)
                ,
                platformContext,
                remember {
                    val iosImagePicker = IosImagePicker(
                        onImagePicked = { imageUri ->
                            uploadNewsfeedViewModel.updateImage(imageUri)
                        },
                        onVideoPicked = { videoUri ->
                            uploadNewsfeedViewModel.updateVideo(videoUri)
                        }
                    )
                    iosImagePicker.imagePicker
                },
                homeViewModel,
                uploadNewsfeedViewModel,
                loadingViewModel,
                updateNew = updateNew,
                onNavigateToHomeScreen = {
                    iosNavigationHandler.navigateTo(IosScreen.HomeScreen.toString())
                }
            )
            IosScreen.UserInformationScreen -> UserInformation.UserInformationScreen(
                platformContext,
                remember {
                    val iosImagePicker = IosImagePicker(
                        onImagePicked = { imageUri ->
                            userInformationViewModel.updateCover(imageUri)
                        },
                        onVideoPicked = {}
                    )
                    iosImagePicker.imagePicker
                },
                selectedUser,
                selectedUser == homeViewModel.currentUser,
                paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                homeViewModel,
                friendViewModel,
                userInformationViewModel,
                onNavigateToShowImageScreen = {image ->
                    selectedImage = image
                    iosNavigationHandler.navigateTo(IosScreen.ShowImageScreen.toString())
                },
                onNavigateToUserInformation = {user ->
                    selectedUser = user
                    iosNavigationHandler.navigateTo(IosScreen.UserInformationScreen.toString())
                },
                navController = iosNavigationHandler,
                onNavigateToUploadNewsfeed = { new ->
                    updateNew = new
                    iosNavigationHandler.navigateTo(IosScreen.UploadNewsScreen.toString())
                },
                onNavigateToCallingScreen = { user ->
                    // iOS calling implementation will be added later
                    // iosNavigationHandler.navigateTo(IosScreen.CallingScreen.toString())
                }
            )
        }
    }
}

@Composable
actual fun SetUpNavigation(context: Any,
                           platformContext: PlatformContext,
                           sessionId: String?,
                           callerId: String?,
                           calleeId: String?) {
    // iOS implementation for notification handling
    // This will be implemented when notification handling is needed
    SetUpNavigation(context, platformContext)
}