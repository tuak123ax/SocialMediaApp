package com.minhtu.firesocialmedia

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.minhtu.firesocialmedia.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.home.Home
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.Comment
import com.minhtu.firesocialmedia.home.navigationscreen.friend.Friend
import com.minhtu.firesocialmedia.home.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.home.navigationscreen.Screen
import com.minhtu.firesocialmedia.home.navigationscreen.Settings
import com.minhtu.firesocialmedia.home.postinformation.PostInformation
import com.minhtu.firesocialmedia.home.search.Search
import com.minhtu.firesocialmedia.home.showimage.ShowImage
import com.minhtu.firesocialmedia.home.uploadnewsfeed.UploadNewsfeed
import com.minhtu.firesocialmedia.home.userinformation.UserInformation
import com.minhtu.firesocialmedia.information.Information
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.signin.SignIn
import com.minhtu.firesocialmedia.signup.SignUp
import com.minhtu.firesocialmedia.signup.SignUpViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.BottomNavigationBar

class Navigation{

}
@Composable
fun SetUpNavigation(context: Any) {
    if(context is Activity) {
        val navController = rememberNavController()
        val startDestination = SignIn.getScreenName()
        var selectedImage = ""
        var selectedUser: UserInstance? = null
        lateinit var selectedNew : NewsInstance

        //Define shared viewModel instance to use for signUp and information screens.
        val signUpViewModel: SignUpViewModel = viewModel()
        //Define shared viewModel instance to use for Home and Search screens.
        val homeViewModel: HomeViewModel = viewModel()
        //Shared instance used for uploadNewFeeds screen.
        var updateNew : NewsInstance? = null
        //Shared instance used for Notification and PostInformation screen.
        var relatedNew : NewsInstance? = null
        val listScreenNeedBottomBar = listOf("HomeScreen", "FriendScreen", "NotificationScreen", "SettingsScreen")
        Scaffold(
            bottomBar = {
                //Bottom navigation bar
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
                if(currentDestination in listScreenNeedBottomBar) {
                    BottomNavigationBar(navController, homeViewModel)
                }
            }
        ) { paddingValues ->
            NavHost(navController = navController, startDestination = startDestination){
                composable(route = SignIn.getScreenName()){
                    SignIn.SignInScreen(context,
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
                    SignUp.SignUpScreen(signUpViewModel,
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
                        signUpViewModel,
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
                        homeViewModel,
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
//                        .paint(
//                            painter = painterResource(id = R.drawable.background),
//                            contentScale = ContentScale.FillBounds)
                        ,
                        homeViewModel,
                        updateNew = updateNew,
                        onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())}
                    )
                }
                composable(route = ShowImage.getScreenName()) {
                    ShowImage.ShowImageScreen(selectedImage, modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black),
                        onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())}
                    )
                }
                composable(route = Search.getScreenName()) {
                    Search.SearchScreen(modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                        hiltViewModel(),
                        homeViewModel,
                        navController,
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
                    UserInformation.UserInformationScreen(selectedUser,
                        paddingValues,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.White),
                        homeViewModel,
                        onNavigateToShowImageScreen = {image ->
                            selectedImage = image
                            navController.navigate(route = ShowImage.getScreenName())},
                        onNavigateToUserInformation = {user ->
                            selectedUser = user
                            navController.navigate(route = UserInformation.getScreenName())},
                        navController = navController,
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
                        showCloseIcon = true,
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
                    ForgotPassword.ForgotPasswordScreen(modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF132026))
//                        .paint(
//                            painter = painterResource(id = R.drawable.background),
//                            contentScale = ContentScale.FillBounds)
                        ,
                        onNavigateToSignInScreen = {navController.navigate(route = SignIn.getScreenName())})
                }
                composable(route = Screen.Friend.route){
                    Friend.FriendScreen(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                        paddingValues = paddingValues,
                        homeViewModel = homeViewModel,
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
                        paddingValues = paddingValues,
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
                        paddingValues = paddingValues,
                        homeViewModel = homeViewModel,
                        onNavigateToSignIn = {
                            navController.navigate(route = SignIn.getScreenName())
                        }
                    )
                }
                composable(route = PostInformation.getScreenName()) {
                    PostInformation.PostInformationScreen(
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
                        navController
                    )
                }
            }
        }

    }
}