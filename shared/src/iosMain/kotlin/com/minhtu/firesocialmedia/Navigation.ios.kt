package com.minhtu.firesocialmedia

import androidx.compose.foundation.background
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
import com.minhtu.firesocialmedia.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.home.Home
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.Comment
import com.minhtu.firesocialmedia.home.comment.CommentViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.Settings
import com.minhtu.firesocialmedia.home.navigationscreen.friend.Friend
import com.minhtu.firesocialmedia.home.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.home.postinformation.PostInformation
import com.minhtu.firesocialmedia.home.search.Search
import com.minhtu.firesocialmedia.home.search.SearchViewModel
import com.minhtu.firesocialmedia.home.showimage.ShowImage
import com.minhtu.firesocialmedia.home.showimage.ShowImageViewModel
import com.minhtu.firesocialmedia.home.uploadnewsfeed.UploadNewfeedViewModel
import com.minhtu.firesocialmedia.home.uploadnewsfeed.UploadNewsfeed
import com.minhtu.firesocialmedia.home.userinformation.UserInformation
import com.minhtu.firesocialmedia.home.userinformation.UserInformationViewModel
import com.minhtu.firesocialmedia.information.Information
import com.minhtu.firesocialmedia.information.InformationViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.signin.SignIn
import com.minhtu.firesocialmedia.signin.SignInViewModel
import com.minhtu.firesocialmedia.signup.SignUp
import com.minhtu.firesocialmedia.signup.SignUpViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.BottomNavigationBar


@Composable
actual fun SetUpNavigation(context: Any) {
    val platformContext = PlatformContext()
    var selectedImage = ""
    var selectedUser: UserInstance? = null
    lateinit var selectedNew : NewsInstance

    //Define shared viewModel instance to use for signUp and information screens.
    val signUpViewModel by lazy { SignUpViewModel() }
    val signInViewModel by lazy { SignInViewModel() }
    val loadingViewModel by lazy { LoadingViewModel() }
    val forgotPasswordViewModel by lazy { ForgotPasswordViewModel() }
    val commentViewModel by lazy { CommentViewModel() }
    val searchViewModel by lazy { SearchViewModel() }
    val uploadNewsfeedViewModel by lazy { UploadNewfeedViewModel() }
    val informationViewModel by lazy { InformationViewModel() }
    val friendViewModel by lazy { FriendViewModel() }
    val userInformationViewModel by lazy { UserInformationViewModel() }
    val showImageViewModel by lazy { ShowImageViewModel() }
    //Define shared viewModel instance to use for Home and Search screens.
    val homeViewModel by lazy { HomeViewModel() }
    //Shared instance used for uploadNewFeeds screen.
    var updateNew : NewsInstance? = null
    //Shared instance used for Notification and PostInformation screen.
    var relatedNew : NewsInstance? = null
    val listScreenNeedBottomBar = listOf(IosScreen.HomeScreen,
        IosScreen.FriendScreen,
        IosScreen.NotificationScreen,
        IosScreen.SettingsScreen)
    val currentScreen = remember { mutableStateOf<IosScreen>(IosScreen.SignInScreen) }
    val iosNavigationHandler = IOSNavigationHandlerWithStack(currentScreen)
    val bottomInset = getBottomSafeAreaInset()
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
                        .fillMaxWidth()
                        .height(60.dp + bottomInset) // Increase height
                        .padding(bottom = bottomInset) // Push content up
                        .background(Color.White)
                )
            }
        }
    ){
        paddingValues ->
        logMessage("Navigation", currentScreen.value.toString())
        when (currentScreen.value) {
            IosScreen.CommentScreen -> Comment.CommentScreen(modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White),
                platformContext,
                showCloseIcon = true,
                commentViewModel,
                currentUser = homeViewModel.currentUser!!,
                selectedNew = selectedNew,
                listUsers = homeViewModel.listUsers,
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
                }
            )
            IosScreen.InformationScreen -> Information.InformationScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF132026))
                ,
                platformContext,
                remember {
                    val iosImagePicker = IosImagePicker { imageUri ->
                        informationViewModel.updateAvatar(imageUri)
                    }
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
            IosScreen.NotificationScreen -> Notification.NotificationScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                platformContext,
                paddingValues = paddingValues,
                searchViewModel = searchViewModel,
                homeViewModel = homeViewModel,
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
                    val iosImagePicker = IosImagePicker { imageUri ->
                        uploadNewsfeedViewModel.updateImage(imageUri)
                    }
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
                    val iosImagePicker = IosImagePicker { imageUri ->
                        informationViewModel.updateAvatar(imageUri)
                    }
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
                }
            )
        }
    }
}