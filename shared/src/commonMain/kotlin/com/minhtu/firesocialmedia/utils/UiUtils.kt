package com.minhtu.firesocialmedia.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidedValue
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
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.presentation.home.Home
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.Screen
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.Friend
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.Settings
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UiUtils {
    companion object{
        @Composable
        fun NewsCard(
            news: NewsInstance,
            user : UserInstance,
            isLiked : Boolean,
            likeCountList : HashMap<String, Int>,
            commentCountList : HashMap<String, Int>,
            localImageLoaderValue : ProvidedValue<*>,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance) -> Unit,
            homeViewModel: HomeViewModel,
            listState : LazyListState,
            onDelete: (action : String, new : NewsInstance) -> Unit,
            onNavigateToCreatePost : (updateNew : NewsInstance) -> Unit) {
            LaunchedEffect(Unit) {
                homeViewModel.updateLikeStatus()
            }

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
                                onNavigateToUserInformation(user)
                            }){
                        CompositionLocalProvider(
                            localImageLoaderValue
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
                                    backgroundColor = "#FFFFFFFF",
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
                            localImageLoaderValue
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
                            text = "Like: ${likeCountList[news.id] ?: 0}",
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Comment: ${commentCountList[news.id] ?: 0}",
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, start = 10.dp, end = 10.dp)) {
                        Button(onClick = {
                            homeViewModel.clickLikeButton(news)
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
                                backgroundColor = if(isLiked) "#00FFFF" else "#FFFFFFFF",
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
                                backgroundColor = "#FFFFFFFF",
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
        fun ShowBasicAlertDialog(
            title : String,
            text : String,
            onClickConfirm: () -> Unit,
            onClickReject: () -> Unit,
            showDialog: MutableState<Boolean>
        ) {
            CommonBackHandler {
                showDialog.value = true
            }

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(title) },
                    text = { Text(text) },
                    confirmButton = {
                        Button(onClick = {
                            onClickConfirm()
                            showDialog.value = false
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDialog.value = false
                            onClickReject()
                        }) {
                            Text("No")
                        }
                    }
                )
            }
        }


        @Composable
        fun BottomNavigationBar(
            currentRoute: String?,
            onNavigate: (String) -> Unit,
            homeViewModel: HomeViewModel,
            onNavigateToUploadNews: () -> Unit,
            modifier: Modifier,
            useDefaultInsets: Boolean = true,
            useCustomBar: Boolean = false
        ) {
            val items = listOf(
                Screen.Home,
                Screen.Friend,
                Screen.Notification,
                Screen.Settings
            )
            if (useCustomBar) {
                // Lightweight custom bar (fixed height) to match Android visual size
                Box(modifier = modifier) {
                    val currentRoute = currentRoute
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color.White)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { screen ->
                            val notificationCount = homeViewModel.listNotificationOfCurrentUser.size
                            val showBadge = screen.route == Notification.getScreenName() && notificationCount > 0
                            val selected = currentRoute == screen.route
                            val tint = if (selected) Color.Red else Color(0xFF666666)
                            val testTag = when(screen.route) {
                                Notification.getScreenName() -> TestTag.TAG_NOTIFICATION_BOTTOM
                                Home.getScreenName() -> TestTag.TAG_HOME_BOTTOM
                                Friend.getScreenName() -> TestTag.TAG_FRIEND_BOTTOM
                                Settings.getScreenName() -> TestTag.TAG_SETTING_BOTTOM
                                else -> ""
                            }
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .testTag(testTag)
                                    .semantics { contentDescription = testTag }
                                    .clickable { onNavigate(screen.route) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (showBadge) {
                                    BadgedBox(badge = { Badge { Text(notificationCount.toString()) } }) {
                                        Icon(
                                            screen.icon,
                                            contentDescription = screen.title,
                                            tint = tint,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        screen.icon,
                                        contentDescription = screen.title,
                                        tint = tint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Floating action button centered above the bar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .offset(y = (-30).dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = listOf(Color.Red, Color.White))
                            )
                            .align(Alignment.BottomCenter)
                    ) {
                        FloatingActionButton(
                            onClick = { onNavigateToUploadNews() },
                            shape = CircleShape,
                            containerColor = Color.Transparent,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                        }
                    }
                }
                return
            }
            Box(modifier = modifier){
                val barInsets = if (useDefaultInsets) androidx.compose.material3.NavigationBarDefaults.windowInsets else androidx.compose.foundation.layout.WindowInsets(0)
                NavigationBar(
                    containerColor = Color.White,
                    windowInsets = barInsets
                ) {
                    val currentRoute = currentRoute
                    items.forEach { screen ->
                        val notificationCount = homeViewModel.listNotificationOfCurrentUser.size

                        val showBadge = screen.route == Notification.getScreenName() && notificationCount > 0
                        val testTag = when(screen.route) {
                            Notification.getScreenName() -> TestTag.TAG_NOTIFICATION_BOTTOM
                            Home.getScreenName() -> TestTag.TAG_HOME_BOTTOM
                            Friend.getScreenName() -> TestTag.TAG_FRIEND_BOTTOM
                            Settings.getScreenName() -> TestTag.TAG_SETTING_BOTTOM
                            else -> ""
                        }
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
                            onClick = { onNavigate(screen.route) },
                            modifier = Modifier
                                .testTag(testTag)
                                .semantics {
                                    contentDescription = testTag
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
        fun BackAndMoreOptionsRow(navigateBack : () -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp)){
                CrossPlatformIcon(
                    icon = "arrow_back",
                    backgroundColor = "#FFFFFFFF",
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
                            navigateBack()
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
                CrossPlatformIcon(
                    icon = "more_horiz",
                    backgroundColor = "#FFFFFFFF",
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
        fun TabLayout(
            listState: LazyListState,
            tabTitles : List<String>,
            localImageLoaderValue : ProvidedValue<*>,
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
                        var searchList by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
                        // Filtered List
                        LaunchedEffect(searchViewModel.query) {
                            searchList = homeViewModel.searchUserByName(searchViewModel.query)
                        }
                        LazyColumn(modifier = Modifier
                            .testTag(TestTag.TAG_PEOPLE_COLUMN)
                            .semantics {
                                contentDescription = TestTag.TAG_PEOPLE_COLUMN
                            }
                        ) {
                            items(searchList){user ->
                                UserRow(user,localImageLoaderValue, onNavigateToUserInformation)
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
                            LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                                localImageLoaderValue,
                                listState,
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
        fun UserRow(user : UserInstance,
                    localImageLoaderValue : ProvidedValue<*>,
                    onNavigateToUserInformation: (user: UserInstance) -> Unit) {
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
                    localImageLoaderValue
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
        fun FriendRequest(localImageLoaderValue : ProvidedValue<*>,
                          requester : UserInstance,
                          currentUser : UserInstance,
                          onNavigateToUserInformation: (user: UserInstance) -> Unit,
                          friendViewModel: FriendViewModel) {
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
                    localImageLoaderValue
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
                            friendViewModel.acceptFriendRequest(requester, currentUser)
                        },
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            colors = ButtonDefaults.buttonColors(Color.Cyan),
                            modifier = Modifier.height(35.dp).weight(1f)){
                            Text(text = "Accept", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = {
                            friendViewModel.rejectFriendRequest(requester, currentUser)
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
        fun LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
            localImageLoaderValue : ProvidedValue<*>,
            listState: LazyListState,
            homeViewModel: HomeViewModel,
            list : List<NewsInstance>,
            onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
            onNavigateToShowImageScreen: (image : String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit) {
            val coroutineScope = rememberCoroutineScope()
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()
            val loadedUsers by homeViewModel.loadedUserState.collectAsState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8E8E8))
                    .testTag(TestTag.TAG_POSTS_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POSTS_COLUMN
                    },
                state = listState
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
                        val user = loadedUsers[news.posterId]
                        if(user != null) {
                            NewsCard(
                                news = news,
                                user = user,
                                isLiked = likeStatus.containsKey(news.id),
                                likeCountList.value,
                                commentCountList.value,
                                localImageLoaderValue,
                                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                onNavigateToUserInformation = onNavigateToUserInformation,
                                homeViewModel = homeViewModel,
                                listState = listState,
                                onDelete = { action, deletedNews ->
                                    isVisible = false
                                    coroutineScope.launch {
                                        delay(250)
                                        homeViewModel.deleteOrHideNew(action, deletedNews)
                                    }
                                },
                                onNavigateToUploadNews
                            )
                        }
                    }
                }

                // Loading row at the bottom
                if (homeViewModel.isLoadingMore.value) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ThreeDotsLoading(
                                modifier = Modifier.padding(bottom = 10.dp),
                                dotSize = 10.dp,
                                dotColor = Color.Blue,
                                spaceBetween = 5.dp
                            )
                        }
                    }
                }
            }
        }

        @Composable
        fun ThreeDotsLoading(
            modifier: Modifier = Modifier,
            dotSize: Dp = 8.dp,
            dotColor: Color = MaterialTheme.colorScheme.primary,
            spaceBetween: Dp = 4.dp,
            animationDelay: Int = 200
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "dotsAnimation")
            val delays = listOf(0, animationDelay, animationDelay * 2)

            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(spaceBetween),
                verticalAlignment = Alignment.CenterVertically
            ) {
                delays.forEach { delayMs ->
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = animationDelay * 2,
                                delayMillis = delayMs,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .scale(scale)
                            .background(dotColor, CircleShape)
                    )
                }
            }
        }

        @Composable
        fun MySnackBarHost(hostState: SnackbarHostState, positive: Boolean?) {
            if(positive != null) {
                SnackbarHost(hostState = hostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color.White,
                        contentColor = if (positive) Color.Green else Color.Red,
                        dismissActionContentColor = Color.Black
                    )
                }
            }
        }
    }
}