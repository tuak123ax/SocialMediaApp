package com.minhtu.firesocialmedia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.crypto.CryptoHelper
import com.minhtu.firesocialmedia.home.Home
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.search.Search
import com.minhtu.firesocialmedia.home.showimage.ShowImage
import com.minhtu.firesocialmedia.home.uploadnewsfeed.UploadNewsfeed
import com.minhtu.firesocialmedia.home.userinformation.UserInformation
import com.minhtu.firesocialmedia.information.Information
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.remoteconfig.FetchResultCallback
import com.minhtu.firesocialmedia.services.remoteconfig.RemoteConfigHelper
import com.minhtu.firesocialmedia.signin.SignIn
import com.minhtu.firesocialmedia.signup.SignUp
import com.minhtu.firesocialmedia.signup.SignUpViewModel
import com.minhtu.firesocialmedia.ui.theme.FireNotebookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireNotebookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }

    @Composable
    private fun MainApp(){
        RemoteConfigHelper.getRemoteConfig()
        fetchDataFromRemoteConfig(object : FetchResultCallback {
            override fun fetchSuccess() {
                Log.e("Fetch", "Fetch success")
            }

            override fun fetchFail() {
                Log.e("Fetch", "Fetch fail")
            }
        })
        askNotificationPermission()
        SetUpNavigation()
    }
    @Composable
    private fun SetUpNavigation(){
        val navController = rememberNavController()
        val startDestination = SignIn.getScreenName()
        var selectedImage = ""
        var selectedUser: UserInstance? = null

        //Define shared viewModel instance to use for signUp and information screens.
        val signUpViewModel: SignUpViewModel = viewModel()
        //Define shared viewModel instance to use for Home and Search screens.
        val homeViewModel: HomeViewModel = viewModel()

        NavHost(navController = navController, startDestination = startDestination){
            composable(route = SignIn.getScreenName()){
                SignIn.SignInScreen(this@MainActivity,
                    modifier = Modifier
                        .fillMaxSize()
                        .paint(
                            painter = painterResource(id = R.drawable.background),
                            contentScale = ContentScale.FillBounds
                        ),
                    onNavigateToSignUpScreen = {navController.navigate(route = SignUp.getScreenName())},
                    onNavigateToHomeScreen = {navController.navigate(route = Home.getScreenName())},
                    onNavigateToInformationScreen = {navController.navigate(route = Information.getScreenName())}
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
                        navController.navigate(route = UserInformation.getScreenName())}
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
                    .background(color = Color.Black)
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
        }
    }
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun fetchDataFromRemoteConfig(fetchResultCallback: FetchResultCallback) {
        RemoteConfigHelper.fetchAndActiveConfig(RemoteConfigHelper.getRemoteConfig(), fetchResultCallback)
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun GreetingPreview() {
        FireNotebookTheme {

        }
    }
}