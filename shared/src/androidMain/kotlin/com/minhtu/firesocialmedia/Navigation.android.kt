package com.minhtu.firesocialmedia

import android.app.Activity
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.home.Home
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.Comment
import com.minhtu.firesocialmedia.home.comment.CommentViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.Screen
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
import com.minhtu.firesocialmedia.signin.SignInState
import com.minhtu.firesocialmedia.signin.SignInViewModel
import com.minhtu.firesocialmedia.signup.SignUp
import com.minhtu.firesocialmedia.signup.SignUpViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.BottomNavigationBar

@Composable
actual fun SetUpNavigation(context: Any) {
    if(context is Activity) {
        val platformContext = PlatformContext(context)
        val navController = rememberNavController()
        val androidNavigationHandler = remember { AndroidNavigationHandler(navController) }
        androidNavigationHandler.ObserveCurrentRoute()
        val startDestination = SignIn.getScreenName()
        var selectedImage = ""
        var selectedUser: UserInstance? = null
        lateinit var selectedNew : NewsInstance

        //Define shared viewModel instance to use for signUp and information screens.
        val signUpViewModel: SignUpViewModel = viewModel()
        val signInViewModel: SignInViewModel = viewModel()
        val loadingViewModel: LoadingViewModel = viewModel()
        val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
        val commentViewModel : CommentViewModel = viewModel()
        val searchViewModel : SearchViewModel = viewModel()
        val uploadNewsfeedViewModel : UploadNewfeedViewModel = viewModel()
        val informationViewModel : InformationViewModel = viewModel()
        val friendViewModel : FriendViewModel = viewModel()
        val userInformationViewModel : UserInformationViewModel = viewModel()
        val showImageViewModel : ShowImageViewModel = viewModel()
        //Define shared viewModel instance to use for Home and Search screens.
        val homeViewModel: HomeViewModel = viewModel()
        //Shared instance used for uploadNewFeeds screen.
        var updateNew : NewsInstance? = null
        //Shared instance used for Notification and PostInformation screen.
        var relatedNew : NewsInstance? = null

        val signInGoogleResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = {
                    result ->
                try{
                    val task = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                    signInViewModel.handleSignInResult(task, platformContext)
                } catch(e : Exception){
                    logMessage("SignIn", "Exception: ${e.message}")
                    signInViewModel.updateSignInStatus(SignInState(false, Constants.LOGIN_ERROR))
                }
            }
        )
        LaunchedEffect(Unit) {
            signInViewModel.setSignInLauncher(object : SignInLauncher{
                override fun launchGoogleSignIn() {
                    val signInRequest = BeginSignInRequest.builder()
                        .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                // Your server's client ID, not your Android client ID.
                                .setServerClientId("744458948813-qktjfopd2cr9b1a87pbr3981ujllb3mt.apps.googleusercontent.com")
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                        .build()
                    val googleSignInClient = Identity.getSignInClient(context)
                    googleSignInClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
                        try {
                            // Launch the One Tap UI
                            val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                            signInGoogleResultLauncher.launch(intentSenderRequest)
                        } catch (e: IntentSender.SendIntentException) {
                            logMessage("OneTapSignIn", "Error launching intent: ${e.localizedMessage}")
                        }
                    }
                        .addOnFailureListener { exception ->
                            logMessage("OneTapSignIn", "Sign-in failed: ${exception.localizedMessage}")
                        }
                }

            })
        }
        val listScreenNeedBottomBar = listOf("HomeScreen", "FriendScreen", "NotificationScreen", "SettingsScreen")

        Scaffold(
            bottomBar = {
                //Bottom navigation bar
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
                if(currentDestination in listScreenNeedBottomBar) {
                    BottomNavigationBar(androidNavigationHandler,
                        homeViewModel,
                        onNavigateToUploadNews = {
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        },
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color.White))
                }
            }
        ) { paddingValues ->
            NavHost(navController = navController, startDestination = startDestination){
                composable(route = SignIn.getScreenName()){
                    SignIn.SignInScreen(
                        platformContext,
                        signInViewModel,
                        loadingViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF132026))
//                            .paint(
//                                painter = painterResource(id = R.drawable.background),
//                                contentScale = ContentScale.FillBounds)
                        ,
                        onNavigateToSignUpScreen = {navController.navigate(route = SignUp.getScreenName())},
                        onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())},
                        onNavigateToInformationScreen = {navController.navigate(route = Information.getScreenName())},
                        onNavigateToForgotPasswordScreen = {navController.navigate(route = ForgotPassword.getScreenName())}
                    )
                }
                composable(route = SignUp.getScreenName()){
                    SignUp.SignUpScreen(
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
                        onNavigateToSignInScreen = {navController.navigate(route = SignIn.getScreenName())},
                        onNavigateToInformationScreen = {navController.navigate(route = Information.getScreenName())})
                }
                composable(route = Information.getScreenName()){
                    Information.InformationScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF132026))
                        ,
                        platformContext,
                        remember {
                            AndroidImagePicker(
                                onImagePicked = { imageUri ->
                                    informationViewModel.updateAvatar(imageUri)
                                },
                                onVideoPicked = {
                                }
                            )
                        },
                        signUpViewModel,
                        informationViewModel,
                        loadingViewModel,
                        onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())}
                    )
                }
                composable(route = Home.getScreenName()){
                    Home.HomeScreen(
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
                            navController.navigate(route = UploadNewsfeed.getScreenName())},
                        onNavigateToShowImageScreen = {image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())},
                        onNavigateToSearch = {navController.navigate(route = Search.getScreenName())},
                        onNavigateToSignIn = {navController.navigate(route = SignIn.getScreenName())},
                        onNavigateToUserInformation = {user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())},
                        onNavigateToCommentScreen = { new ->
                            selectedNew = new
                            navController.navigate(route = Comment.getScreenName())
                        }
                    )
                }
                composable(route = UploadNewsfeed.getScreenName()){
                    UploadNewsfeed.UploadNewsfeedScreen(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
//                        .paint(
//                            painter = painterResource(id = R.drawable.background),
//                            contentScale = ContentScale.FillBounds)
                        ,
                        platformContext,
                        remember {
                            AndroidImagePicker(
                                onImagePicked = { imageUri ->
                                    uploadNewsfeedViewModel.updateImage(imageUri)
                                },
                                onVideoPicked = { videoUri ->
                                    uploadNewsfeedViewModel.updateVideo(videoUri)
                                }
                            )
                        },
                        homeViewModel,
                        uploadNewsfeedViewModel,
                        loadingViewModel,
                        updateNew = updateNew,
                        onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())}
                    )
                }
                composable(route = ShowImage.getScreenName()) {
                    ShowImage.ShowImageScreen(
                        platformContext,
                        selectedImage,
                        showImageViewModel = showImageViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.Black),
                        onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())}
                    )
                }
                composable(route = Search.getScreenName()) {
                    Search.SearchScreen(modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                        platformContext,
                        searchViewModel,
                        homeViewModel,
                        androidNavigationHandler,
                        onNavigateToUserInformation = {user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())},
                        onNavigateToShowImageScreen = {image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())},
                        onNavigateToCommentScreen = { new ->
                            selectedNew = new
                            navController.navigate(route = Comment.getScreenName())
                        },
                        onNavigateToUploadNewsFeed = { new ->
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        }
                    )
                }
                composable(route = UserInformation.getScreenName()){
                    UserInformation.UserInformationScreen(
                        platformContext,
                        remember {
                            AndroidImagePicker(
                                onImagePicked = { imageUri ->
                                    userInformationViewModel.updateCover(imageUri)
                                },
                                onVideoPicked = {
                                }
                            )
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
                            navController.navigate(route = ShowImage.getScreenName())},
                        onNavigateToUserInformation = {user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())},
                        navController = androidNavigationHandler,
                        onNavigateToUploadNewsfeed = { new ->
                            updateNew = new
                            navController.navigate(route = UploadNewsfeed.getScreenName())
                        }
                    )
                }
                composable(route = Comment.getScreenName()) {
                    Comment.CommentScreen(modifier = Modifier
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
                            navController.navigate(route = ShowImage.getScreenName())},
                        onNavigateToUserInformation = {user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())}) {
                        navController.navigate(route = Home.getScreenName())
                    }
                }
                composable(route = ForgotPassword.getScreenName()) {
                    ForgotPassword.ForgotPasswordScreen(
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
                        onNavigateToSignInScreen = {navController.navigate(route = SignIn.getScreenName())})
                }
                composable(route = Screen.Friend.route){
                    Friend.FriendScreen(
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
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateToShowImageScreen = {
                                image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        })
                }
                composable(route = Screen.Notification.route){
                    Notification.NotificationScreen(
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
                            navController.navigate(route = PostInformation.getScreenName())
                        },
                        onNavigateToUserInformation = {
                                user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        }
                    )
                }
                composable(route = Screen.Settings.route){
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
                composable(route = PostInformation.getScreenName()) {
                    PostInformation.PostInformationScreen(
                        platformContext,
                        relatedNew!!,
                        onNavigateToShowImageScreen = {
                                image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())
                        },
                        onNavigateToUserInformation = {
                                user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())
                        },
                        onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())},
                        homeViewModel,
                        commentViewModel,
                        androidNavigationHandler
                    )
                }
            }
        }
    }
}