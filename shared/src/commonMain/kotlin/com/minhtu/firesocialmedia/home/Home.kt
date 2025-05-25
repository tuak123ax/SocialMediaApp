package com.minhtu.firesocialmedia.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.CrossPlatformIcon
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.generateImageLoader
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.ShowAlertDialogToLogout
import com.minhtu.firesocialmedia.utils.Utils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

class Home {
    companion object{
        @Composable
        fun HomeScreen(modifier: Modifier,
                       platform : PlatformContext,
                       homeViewModel: HomeViewModel,
                       loadingViewModel: LoadingViewModel,
                       paddingValues: PaddingValues,
                       onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
                       onNavigateToShowImageScreen: (image : String) -> Unit,
                       onNavigateToSearch: () -> Unit,
                       onNavigateToSignIn: () -> Unit,
                       onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                       onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val commentStatus by homeViewModel.commentStatus.collectAsState()

            val showDialog = remember { mutableStateOf(false) }
            ShowAlertDialogToLogout( onClickConfirm = {
                homeViewModel.clearAccountInStorage(platform)
            },
                onNavigateToSignIn,
                showDialog)


            //Observe Live Data as State
            val usersList by  homeViewModel.allUsers.collectAsState()

            val numberOfLists by remember { derivedStateOf { homeViewModel.numberOfListNeedToLoad } }

            val newsList = homeViewModel.allNews.collectAsState()

            val currentUserState = homeViewModel.currentUserState

            var isAllUsersVisible by remember { mutableStateOf(true) }

            LaunchedEffect(usersList) {
                loadingViewModel.showLoading()
                //Load users list and news list.
                Utils.getAllUsers(homeViewModel, platform)
                Utils.getAllNews(homeViewModel, platform)
                homeViewModel.decreaseNumberOfListNeedToLoad(1)
                if(numberOfLists == 0) {
                    loadingViewModel.hideLoading()
                }
            }
            LaunchedEffect(newsList.value) {
                homeViewModel.decreaseNumberOfListNeedToLoad(1)
                if (numberOfLists == 0) {
                    loadingViewModel.hideLoading()
                }
            }
            LaunchedEffect(commentStatus){
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(verticalArrangement = Arrangement.Top, modifier = modifier.padding(paddingValues)) {
                    //App name and buttons
                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                    ) {
                        Text(
                            text = "FireSocialMedia",
                            color = Color.Red,
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(35.dp) // Outer box size
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color.Black, CircleShape)
                                .clickable {
                                    onNavigateToSearch()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CrossPlatformIcon(
                                icon = "search",
                                color = "#FFFFFFFF",
                                contentDescription = "Search Icon",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(25.dp) // Reduce to prevent touching the border
                                    .padding(2.dp) // Ensures space between image and border
                                    .testTag(TestTag.TAG_ICON_BUTTON_SEARCH)
                                    .semantics{
                                        contentDescription = TestTag.TAG_ICON_BUTTON_SEARCH
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(15.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(35.dp) // Set exact size
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color.Black, CircleShape)
                                .clickable {
                                    showDialog.value = true
                                }
                        ) {
                            CrossPlatformIcon(
                                icon = "logout",
                                color = "#FFFFFFFF",
                                contentDescription = "Logout Icon",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(2.dp) // Ensures space between image and border
                                    .testTag(TestTag.TAG_ICON_BUTTON_LOGOUT)
                                    .semantics{
                                        contentDescription = TestTag.TAG_ICON_BUTTON_LOGOUT
                                    }
                            )
                        }
                    }

                    //Create post bar and other users
                    AnimatedVisibility(visible = isAllUsersVisible) {
                        Column(verticalArrangement = Arrangement.Top){
                            Row(horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()){
                                //Current user avatar
                                if (currentUserState != null) {
                                    val userImage = currentUserState.image // Avoid force unwrapping

                                    CompositionLocalProvider(
                                        LocalImageLoader provides remember { generateImageLoader() },
                                    ) {
                                        AutoSizeImage(
                                            userImage,
                                            contentDescription = "Poster Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .padding(top = 10.dp, start = 10.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    onNavigateToUserInformation(homeViewModel.currentUser)
                                                }
                                                .testTag(TestTag.TAG_CURRENT_USER)
                                                .semantics{
                                                    contentDescription = TestTag.TAG_CURRENT_USER
                                                }
                                        )
                                    }
                                }

                                //Create post
                                OutlinedTextField(
                                    value = "", onValueChange = {
                                    }, modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .clickable {
                                            onNavigateToUploadNews(null)
                                        }
                                        .testTag(TestTag.TAG_CREATE_POST)
                                        .semantics{
                                            contentDescription = TestTag.TAG_CREATE_POST
                                        },
                                    label = { Text(text = "What are you thinking?")},
                                    enabled = false,    // Disables the TextField
                                    singleLine = true,
                                    shape = RoundedCornerShape(30.dp)
                                )
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .testTag(TestTag.TAG_USERS_ROW)
                                .semantics{
                                    contentDescription = TestTag.TAG_USERS_ROW
                                }) {
                                usersList.forEach { user ->
                                    item {
                                        UserCard(user = user, onNavigateToUserInformation = onNavigateToUserInformation)
                                    }
                                }
                            }
                        }
                    }

                    val listState = homeViewModel.listState
                    // LaunchedEffect to track the scroll state
                    LaunchedEffect(Unit) {
                        snapshotFlow { listState.firstVisibleItemIndex}
                            .distinctUntilChanged()
                            .collectLatest { index ->
                                isAllUsersVisible = index == 0  // Show only if scrolled to the top
                            }
                    }
                    //Newsfeed
                    val sortedNewsList by remember {
                        derivedStateOf {
                            newsList.value.sortedByDescending { it.timePosted }
                        }
                    }
                    UiUtils.LazyColumnOfNewsWithSlideOutAnimation(
                        platform,
                        homeViewModel,
                        sortedNewsList,
                        onNavigateToUploadNews,
                        onNavigateToShowImageScreen,
                        onNavigateToUserInformation)
                }
                if(isLoading){
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        private fun UserCard(user: UserInstance, onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Card(
                modifier = Modifier.size(70.dp, 90.dp)
                    .testTag(TestTag.TAG_ITEM_IN_ROW)
                    .semantics{
                        contentDescription = TestTag.TAG_ITEM_IN_ROW
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(
                        LocalImageLoader provides remember { generateImageLoader() },
                    ) {
                        AutoSizeImage(
                            user.image,
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f) // Allocates equal space to the image and text
                                .clip(CircleShape)
                                .clickable {
                                    // Handle image click
                                    onNavigateToUserInformation(user)
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp)) // Optional spacing between image and text
                    Text(
                        text = user.name,
                        color = Color.Black,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis, // Adds "..." at the end if the text overflows
                        modifier = Modifier.padding(horizontal = 4.dp) // Adds padding around text
                    )
                }
            }
        }

        fun getScreenName(): String{
            return "HomeScreen"
        }
    }

}