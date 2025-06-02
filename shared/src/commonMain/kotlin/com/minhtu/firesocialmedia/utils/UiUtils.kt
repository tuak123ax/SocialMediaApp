package com.minhtu.firesocialmedia.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.CommonBackHandler
import com.minhtu.firesocialmedia.CrossPlatformIcon
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.VideoPlayer
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.convertTimeToDateString
import com.minhtu.firesocialmedia.generateImageLoader
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.Screen
import com.minhtu.firesocialmedia.home.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.home.search.SearchViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.logMessage
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UiUtils {
    companion object{
        @Composable
        fun NewsCard(platform : PlatformContext,
                     news: NewsInstance,
                     onNavigateToShowImageScreen: (image: String) -> Unit,
                     onNavigateToUserInformation: (user: UserInstance) -> Unit,
                     homeViewModel: HomeViewModel,
                     listState : LazyListState,
                     onDelete: (action : String, new : NewsInstance) -> Unit,
                     onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit) {
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val isLiked = likeStatus.contains(news.id)
            LaunchedEffect(likeStatus) {
                homeViewModel.updateLikeStatus()
            }
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()
            Card(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 5.dp)
                    .fillMaxWidth()
                    .testTag(TestTag.TAG_POST_IN_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POST_IN_COLUMN
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.background(color = Color.White).padding(10.dp).fillMaxWidth()
                            .clickable {
                                var user =
                                    Utils.findUserById(news.posterId, homeViewModel.listUsers)
                                if(user == null) {
                                    user = homeViewModel.currentUser
                                }
                                if(user != null) {
                                    onNavigateToUserInformation(user)
                                }
                            }){
                        CompositionLocalProvider(
                            LocalImageLoader provides remember { generateImageLoader() },
                        ) {
                            AutoSizeImage(
                                news.avatar,
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .testTag(TestTag.TAG_POSTER_AVATAR)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POSTER_AVATAR
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        //Box to add three dot icon and dropdownMenu when clicking the icon
                        Box{
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = {
                                showMenu = true
                            }) {
                                CrossPlatformIcon(
                                    icon = "more_horiz",
                                    color = "#FFFFFFFF",
                                    contentDescription = "More Options",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                                        .semantics {
                                            contentDescription = TestTag.TAG_BUTTON_MOREOPTIONS
                                        }
                                )
                            }
                            DropdownMenuForResponse(showMenu, homeViewModel, news,{showMenu = false}, listState , onDelete, onNavigateToCreatePost)
                        }
                    }
                    ExpandableText(news.message)
                    if(news.image.isNotEmpty()){
                        CompositionLocalProvider(
                            LocalImageLoader provides remember { generateImageLoader() },
                        ) {
                            AutoSizeImage(
                                news.image,
                                contentDescription = "Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(5.dp)
                                    .clickable {
                                        onNavigateToShowImageScreen(news.image)
                                    }
                                    .testTag(TestTag.TAG_POST_IMAGE)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POST_IMAGE
                                    }
                            )
                        }
                    } else {
                        if(news.video.isNotEmpty()) {
                            VideoPlayer(news.video,
                                Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(5.dp)
                                    .testTag(TestTag.TAG_POST_VIDEO)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POST_VIDEO
                                    })
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Start) {
                        Text(
                            text = "Like: ${likeCountList.value[news.id] ?: 0}",
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Comment: ${commentCountList.value[news.id] ?: 0}",
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, start = 10.dp, end = 10.dp)) {
                        Button(onClick = {
                            homeViewModel.clickLikeButton(news, platform)
                        },
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            colors = if(isLiked) ButtonDefaults.buttonColors(Color.Cyan)
                            else ButtonDefaults.buttonColors(Color.White),
                            modifier = Modifier.height(35.dp).weight(1f)
                                .testTag(TestTag.TAG_BUTTON_LIKE)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_LIKE
                                }){
                            CrossPlatformIcon(
                                icon = "like",
                                color = if(isLiked) "#00FFFF" else "#FFFFFFFF",
                                contentDescription = "Like",
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = if(isLiked) "Liked" else "Like", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = {
                            homeViewModel.clickCommentButton(news)
                        },
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            colors = ButtonDefaults.buttonColors(Color.White),
                            modifier = Modifier.height(35.dp).weight(1f)
                                .testTag(TestTag.TAG_BUTTON_COMMENT)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_COMMENT
                                }){
                            CrossPlatformIcon(
                                icon = "comment",
                                color = "#FFFFFFFF",
                                contentDescription = "Comment",
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = "Comment", color = Color.Black)
                        }
                    }
                }
            }
        }

        @Composable
        fun ShowAlertDialog(title : String, message : String, resetAndBack:() -> Unit, showDialog : MutableState<Boolean>) {
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(title) },
                    text = { Text(message) },
                    confirmButton = {
                        Button(
                            onClick = {
                            resetAndBack()
                        },
                            modifier = Modifier
                                .testTag(TestTag.TAG_BUTTON_YES)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_YES
                                }
                            ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog.value = false },
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_NO)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_NO
                                }) {
                            Text("No")
                        }
                    }
                )
            }
        }

        @Composable
        fun ShowAlertDialogToLogout(
            onClickConfirm: () -> Unit,
            onNavigateToSignIn: () -> Unit,
            showDialog: MutableState<Boolean>
        ) {
            val coroutineScope = rememberCoroutineScope()

            CommonBackHandler {
                showDialog.value = true
            }

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to logout?") },
                    confirmButton = {
                        Button(onClick = {
                            onClickConfirm()
                            showDialog.value = false
                            coroutineScope.launch {
                                delay(100) // let the dialog close properly
                                onNavigateToSignIn()
                            }
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


        @Composable
        fun BottomNavigationBar(navHandler: NavigationHandler, homeViewModel: HomeViewModel, onNavigateToUploadNews: () -> Unit, modifier: Modifier) {
            val items = listOf(
                Screen.Home,
                Screen.Friend,
                Screen.Notification,
                Screen.Settings
            )
            Box(
                modifier = modifier
            ){
                NavigationBar(containerColor = Color.White) {
                    val currentRoute = navHandler.getCurrentRoute()
                    logMessage("currentRoute", currentRoute.toString())
                    items.forEach { screen ->
                        val notificationCount = homeViewModel.listNotificationOfCurrentUser.size
                        val showBadge = screen.route == Notification.getScreenName() && notificationCount > 0
                        NavigationBarItem(
                            icon = {
                                if(showBadge) {
                                    BadgedBox(
                                        badge = {
                                            Badge{
                                                Text(notificationCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(screen.icon, contentDescription = screen.title) }
                                    }
                                else {
                                    Icon(screen.icon, contentDescription = screen.title) }
                                },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navHandler.navigateTo(screen.route)
                            }
                        )
                    }
                }

                //Floating action button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .offset(y = (-30).dp)
                        .shadow(8.dp, CircleShape) // Shadow before clipping
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.Red, Color.White)
                            )
                        )
                        .align(Alignment.BottomCenter)
                ) {
                    FloatingActionButton(
                        onClick = {
                            onNavigateToUploadNews()
                        },
                        shape = CircleShape,
                        containerColor = Color.Transparent, // Transparent to let gradient show
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                    }
                }
            }
        }
        @Composable
        fun BackAndMoreOptionsRow(navHandle : NavigationHandler) {
            Row(horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp)){
                CrossPlatformIcon(
                    icon = "arrow_back",
                    color = "#FFFFFFFF",
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag(TestTag.TAG_BUTTON_BACK)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_BACK
                        }
                        .clickable {
                            // Handle back button click
                            navHandle.navigateBack()
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
                CrossPlatformIcon(
                    icon = "more_horiz",
                    color = "#FFFFFFFF",
                    contentDescription = "More Options",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_MOREOPTIONS
                        }
                        .clickable {

                        }
                )
            }
        }

        @Composable
        fun TabLayout(platform : PlatformContext,
                      tabTitles : List<String>,
                      homeViewModel: HomeViewModel,
                      searchViewModel: SearchViewModel,
                      onNavigateToShowImageScreen: (image: String) -> Unit,
                      onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                      onNavigateToUploadNewsfeed : (updateNew : NewsInstance?) -> Unit){
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            Column(modifier = Modifier.fillMaxSize()){
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    indicator = {
                            tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color.Red
                        )
                    }
                ) {
                    tabTitles.forEachIndexed{
                            index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                            },
                            text = {
                                Text(text = title, fontSize = 18.sp)
                            }
                        )
                    }
                }
                when(selectedTabIndex){
                    0 -> {
                        // Filtered List
                        LazyColumn(modifier = Modifier
                            .testTag(TestTag.TAG_PEOPLE_COLUMN)
                            .semantics {
                                contentDescription = TestTag.TAG_PEOPLE_COLUMN
                            }) {
                            val filterList = homeViewModel.listUsers.filter { user ->
                                user.name.contains(searchViewModel.query, ignoreCase = true)
                            }
                            items(filterList){user ->
                                UserRow(user, onNavigateToUserInformation)
                            }
                        }
                    }
                    1 -> {
                        if(searchViewModel.query.isNotEmpty()) {
                            val filterList by remember {
                                derivedStateOf {
                                    homeViewModel.listNews.filter { news ->
                                        news.message.contains(searchViewModel.query, ignoreCase = true)
                                    }
                                }
                            }
                            LazyColumnOfNewsWithSlideOutAnimation(
                                platform,
                                homeViewModel,
                                filterList,
                                onNavigateToUploadNewsfeed,
                                onNavigateToShowImageScreen,
                                onNavigateToUserInformation)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()){
                                Text(text = "Please input content you want to search",
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun UserRow(user : UserInstance, onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToUserInformation(user)
                    }
                    .testTag(TestTag.TAG_FRIEND)
                    .semantics{
                        contentDescription = TestTag.TAG_FRIEND
                    }){
                CompositionLocalProvider(
                    LocalImageLoader provides remember { generateImageLoader() },
                ) {
                    AutoSizeImage(
                        user.image,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier =  Modifier
                            .size(60.dp)
                            .padding(10.dp)
                            .clip(CircleShape)
                    )
                }
                Text(
                    text = user.name,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 5.dp), // Adds padding around text
                    maxLines = 2
                )
            }
        }

        @Composable
        fun FriendRequest(platform : PlatformContext, requester : UserInstance, currentUser : UserInstance, onNavigateToUserInformation: (user: UserInstance) -> Unit, friendViewModel: FriendViewModel) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToUserInformation(requester)
                    }
                    .testTag(TestTag.TAG_FRIEND_REQUEST)
                    .semantics{
                        contentDescription = TestTag.TAG_FRIEND_REQUEST
                    }){
                CompositionLocalProvider(
                    LocalImageLoader provides remember { generateImageLoader() },
                ) {
                    AutoSizeImage(
                        requester.image,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier =  Modifier
                            .size(100.dp)
                            .padding(10.dp)
                            .clip(CircleShape)
                    )
                }
                Column(modifier = Modifier.padding(end = 5.dp)) {
                    Text(
                        text = requester.name,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(5.dp)
                    )
                    Row(horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)){
                        Button(onClick = {
                            friendViewModel.acceptFriendRequest(requester, currentUser, platform)
                        },
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            colors = ButtonDefaults.buttonColors(Color.Cyan),
                            modifier = Modifier.height(35.dp).weight(1f)){
                            Text(text = "Accept", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = {
                            friendViewModel.rejectFriendRequest(requester, currentUser, platform)
                        },
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            colors = ButtonDefaults.buttonColors(Color.White),
                            modifier = Modifier.height(35.dp).weight(1f)){
                            Text(text = "Reject", color = Color.Black)
                        }
                    }
                }
            }
        }

        @Composable
        fun ExpandableText(
            text: String,
            collapsedMaxLines: Int = 3
        ) {
            var isExpanded by remember { mutableStateOf(false) }
            var isOverflowing by remember { mutableStateOf(false) }

            Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                Column {
                    Text(
                        text = text,
                        color = Color.Black,
                        maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult ->
                            isOverflowing = textLayoutResult.hasVisualOverflow
                        },
                        modifier = Modifier.animateContentSize()
                    )

                    if (isOverflowing || isExpanded) {
                        Text(
                            text = if (isExpanded) "See less" else "See more",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isExpanded = !isExpanded }
                        )
                    }
                }
            }
        }

        @Composable
        fun DropdownMenuForResponse(expanded : Boolean,
                                    homeViewModel: HomeViewModel,
                                    selectedNew : NewsInstance,
                                    onDismissRequest: () -> Unit,
                                    listState : LazyListState,
                                    onDelete: (action : String, new : NewsInstance) -> Unit?,
                                    onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                var buttonText = "Hide"
                if(selectedNew.posterId == homeViewModel.currentUser!!.uid) {
                    buttonText = "Delete"
                    DropdownMenuItem(
                        text = { Text("Update") },
                        onClick = {
                            onNavigateToCreatePost(selectedNew)
                            onDismissRequest()
                        }
                    )
                }
                val scope = rememberCoroutineScope()
                DropdownMenuItem(
                    text = { Text(buttonText) },
                    onClick = {
                        scope.launch {
                            val index = listState.firstVisibleItemIndex
                            val offset = listState.firstVisibleItemScrollOffset
                            //Dismiss the menu
                            onDismissRequest()
                            onDelete(buttonText, selectedNew)
                            // Scroll back to where we were
                            listState.scrollToItem(index, offset)
                        }
                    }
                )
            }
        }

        @Composable
        fun LazyColumnOfNewsWithSlideOutAnimation(platform : PlatformContext,
                                                  homeViewModel: HomeViewModel,
                                                  list : List<NewsInstance>,
                                                  onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
                                                  onNavigateToShowImageScreen: (image : String) -> Unit,
                                                  onNavigateToUserInformation: (user: UserInstance?) -> Unit) {
            val coroutineScope = rememberCoroutineScope()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8E8E8))
                    .testTag(TestTag.TAG_POSTS_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POSTS_COLUMN
                    },
                state = homeViewModel.listState
            ) {
                items(
                    items = list,
                    key = { it.id }
                ) { news ->
                    var isVisible by remember(news.id) { mutableStateOf(true) }

                    AnimatedVisibility(
                        visible = isVisible,
                        exit = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 200)
                        )
                    ) {
                        NewsCard(
                            platform,
                            news = news,
                            onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                            onNavigateToUserInformation = onNavigateToUserInformation,
                            homeViewModel = homeViewModel,
                            listState = homeViewModel.listState,
                            onDelete = { action, deletedNews ->
                                isVisible = false
                                coroutineScope.launch {
                                    delay(250)
                                    homeViewModel.deleteOrHideNew(action, deletedNews, platform)
                                }
                            },
                            onNavigateToUploadNews
                        )
                    }
                }
            }
        }
    }
}