package com.minhtu.firesocialmedia.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

class Home {
    companion object{
        @Composable
        fun HomeScreen(modifier: Modifier,
                       homeViewModel: HomeViewModel,
                       loadingViewModel: LoadingViewModel = viewModel(),
                       onNavigateToUploadNews: () -> Unit,
                       onNavigateToShowImageScreen: (image : String) -> Unit,
                       onNavigateToSearch: () -> Unit,
                       onNavigateToSignIn: () -> Unit,
                       onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                       onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit){
            val context = LocalContext.current
            val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)
            val isLoading by loadingViewModel.isLoading.collectAsState()

            val showDialog = remember { mutableStateOf(false) }
            ShowAlertDialogToLogout(context, homeViewModel, onNavigateToSignIn, showDialog)

            val listState = homeViewModel.listState
            var isAllUsersVisible by remember { mutableStateOf(true) }

            // LaunchedEffect to track the scroll state
            LaunchedEffect(Unit) {
                snapshotFlow { listState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collectLatest { index ->
                        isAllUsersVisible = index == 0  // Show only if scrolled to the top
                    }
            }

            LaunchedEffect(lifecycleOwner.value) {
                loadingViewModel.showLoading()
                //Load users list and news list.
                homeViewModel.numberOfListNeedToLoad = 2
                Utils.getAllUsers(homeViewModel)
                Utils.getAllNews(homeViewModel)
                homeViewModel.allUsers.observe(lifecycleOwner.value){ _ ->
                    homeViewModel.decreaseNumberOfListNeedToLoad(1)
                    if(homeViewModel.numberOfListNeedToLoad == 0) {
                        loadingViewModel.hideLoading()
                    }
                }
                homeViewModel.allNews.observe(lifecycleOwner.value){ _ ->
                    homeViewModel.decreaseNumberOfListNeedToLoad(1)
                    if(homeViewModel.numberOfListNeedToLoad == 0) {
                        loadingViewModel.hideLoading()
                    }
                }

                homeViewModel.commentStatus.observe(lifecycleOwner.value) { selectedNew ->
                    Log.e("HomeScreen", "selected new: $selectedNew")
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                    homeViewModel.commentStatus.removeObservers(lifecycleOwner.value)
                }
            }

            val openChatAppIntent = getChatAppIntent(context)
            val openChatAppLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            }

            //Observe Live Data as State
            val usersList =  homeViewModel.allUsers.observeAsState(initial = emptyList())

            val newsList = homeViewModel.allNews.observeAsState(initial = emptyList())

            Box(modifier = Modifier.fillMaxSize()) {
                Column(verticalArrangement = Arrangement.Top, modifier = modifier) {
                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()) {
                        Text(
                            text = "FireSocialMedia",
                            color = Color.Cyan,
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        AsyncImage(model = ImageRequest.Builder(context)
                            .data(R.drawable.fire_chat_icon)
                            .crossfade(true)
                            .build(),
                            contentDescription = "Chat App Icon",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (openChatAppIntent != null) {
                                        openChatAppLauncher.launch(openChatAppIntent)
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "Can't find this app on your device!",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                })
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        AsyncImage(model = ImageRequest.Builder(context)
                            .data(R.drawable.search)
                            .crossfade(true)
                            .build(),
                            contentDescription = "Search Icon",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    onNavigateToSearch()
                                })
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                        AsyncImage(model = ImageRequest.Builder(context)
                            .data(R.drawable.logout)
                            .crossfade(true)
                            .build(),
                            contentDescription = "Logout Icon",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    showDialog.value = true
                                })
                    }

                    AnimatedVisibility(visible = isAllUsersVisible) {
                        Column(verticalArrangement = Arrangement.Top){
                            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()){
                                OutlinedTextField(
                                    value = "", onValueChange = {
                                    }, modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                        .clickable {
                                            onNavigateToUploadNews()
                                        },
                                    label = { Text(text = "What are you thinking?")},
                                    enabled = false,    // Disables the TextField
                                    singleLine = true
                                )
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)) {
                                items(usersList.value){user ->
                                    UserCard(user = user, context, onNavigateToUserInformation)
                                }
                            }
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
                        .fillMaxSize(), state = listState) {
                        items(newsList.value){news ->
                            homeViewModel.addLikeCountData(news.id, news.likeCount)
                            homeViewModel.addCommentCountData(news.id, news.commentCount)
                            UiUtils.NewsCard(news = news, context, onNavigateToShowImageScreen, onNavigateToUserInformation, homeViewModel)
                        }
                    }
                }
                if(isLoading){
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        private fun UserCard(user: UserInstance, context: Context, onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Card(
                modifier = Modifier.size(100.dp, 120.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(user.image))
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .weight(1f) // Allocates equal space to the image and text
                            .clickable {
                                // Handle image click
                                onNavigateToUserInformation(user)
                            }
                    )
                    Spacer(modifier = Modifier.height(1.dp)) // Optional spacing between image and text
                    Text(
                        text = user.name,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis, // Adds "..." at the end if the text overflows
                        modifier = Modifier.padding(horizontal = 4.dp) // Adds padding around text
                    )
                }
            }
        }

        private fun getChatAppIntent(context: Context): Intent? {
            val packageName = "com.example.firechat"
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if(intent != null) {
                return intent
            }
            return null
        }

        @Composable
        private fun ShowAlertDialogToLogout(context : Context, homeViewModel: HomeViewModel, onNavigateToSignIn:() -> Unit, showDialog : MutableState<Boolean>) {
            BackHandler {
                showDialog.value = true
            }
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to logout?") },
                    confirmButton = {
                        Button(onClick = {
                            val account = GoogleSignIn.getLastSignedInAccount(context)
                            if(account != null) {
                                FirebaseAuth.getInstance().signOut()
                            }
                            homeViewModel.clearAccountInStorage(context)
                            onNavigateToSignIn()
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog.value = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }

        fun getScreenName(): String{
            return "HomeScreen"
        }
    }
}