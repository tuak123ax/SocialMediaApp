package com.minhtu.firesocialmedia

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.minhtu.firesocialmedia.forgotpassword.ForgotPassword
import com.minhtu.firesocialmedia.home.Home
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.Comment
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

class Navigation{
    private fun getChatAppIntent(context: Context): Intent? {
        val packageName = "com.example.firechat"
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if(intent != null) {
            return intent
        }
        return null
    }

    fun logout(context: Context, homeViewModel: HomeViewModel, onNavigateToSignIn: () -> Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            FirebaseAuth.getInstance().signOut()
        }
        homeViewModel.clearAccountInStorage(context)
        onNavigateToSignIn()
    }
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

        NavHost(navController = navController, startDestination = startDestination){
            composable(route = SignIn.getScreenName()){
                SignIn.SignInScreen(context,
                    modifier = Modifier
                        .fillMaxSize()
                        .paint(
                            painter = painterResource(id = R.drawable.background),
                            contentScale = ContentScale.FillBounds
                        ),
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
                        .paint(
                            painter = painterResource(id = R.drawable.background),
                            contentScale = ContentScale.FillBounds
                        ),
                    onNavigateToSignInScreen = {navController.navigate(route = SignIn.getScreenName())},
                    onNavigateToInformationScreen = {navController.navigate(route = Information.getScreenName())})
            }
            composable(route = Information.getScreenName()){
                Information.InformationScreen(
                    modifier = Modifier.fillMaxSize(),
                    signUpViewModel,
                    onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())}
                )
            }
            composable(route = Home.getScreenName()){
                Home.HomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .paint(
                            painter = painterResource(id = R.drawable.background),
                            contentScale = ContentScale.FillBounds
                        ),
                    homeViewModel,
                    onNavigateToUploadNews = {navController.navigate(route = UploadNewsfeed.getScreenName())},
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
                    .paint(
                        painter = painterResource(id = R.drawable.background),
                        contentScale = ContentScale.FillBounds
                    ),
                    homeViewModel,
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
                    onNavigateToUserInformation = {user ->
                        selectedUser = user
                        navController.navigate(route = UserInformation.getScreenName())}
                )
            }
            composable(route = UserInformation.getScreenName()){
                UserInformation.UserInformationScreen(selectedUser,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                    homeViewModel,
                    onNavigateToShowImageScreen = {image ->
                        selectedImage = image
                        navController.navigate(route = ShowImage.getScreenName())},
                    onNavigateToUserInformation = {user ->
                        selectedUser = user
                        navController.navigate(route = UserInformation.getScreenName())}
                )
            }
            composable(route = Comment.getScreenName()) {
                Comment.CommentScreen(modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                    currentUser = homeViewModel.currentUser,
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
                    .paint(
                    painter = painterResource(id = R.drawable.background),
                    contentScale = ContentScale.FillBounds),
                    onNavigateToSignInScreen = {navController.navigate(route = SignIn.getScreenName())})
            }
        }
    }
}