package com.minhtu.firesocialmedia.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.Screen
import com.minhtu.firesocialmedia.home.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.home.search.SearchViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance

class UiUtils {
    companion object{
        @Composable
        fun NewsCard(news: NewsInstance, context: Context, onNavigateToShowImageScreen: (image: String) -> Unit, onNavigateToUserInformation: (user: UserInstance) -> Unit, homeViewModel: HomeViewModel) {
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
                    .testTag(TestTag.TAG_POST_IN_COLUMN),
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
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(news.avatar))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Poster Avatar",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag(TestTag.TAG_POSTER_AVATAR)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = Utils.convertTimeToDateString(news.timePosted),
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { /* Handle click */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz, //Horizontal three dots ⋯
                                contentDescription = "More Options",
                                tint = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = news.message,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    if(news.image.isNotEmpty()){
                        AsyncImage(
                            model = news.image,
                            contentDescription = "Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(5.dp)
                                .clickable {
                                    onNavigateToShowImageScreen(news.image)
                                }
                                .testTag(TestTag.TAG_POST_IMAGE)
                        )
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
                            homeViewModel.clickLikeButton(news)
                        },
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            colors = if(isLiked) ButtonDefaults.buttonColors(Color.Cyan)
                            else ButtonDefaults.buttonColors(Color.White),
                            modifier = Modifier.height(35.dp).weight(1f).testTag(TestTag.TAG_BUTTON_LIKE)){
                            Image(
                                painter = painterResource(id = R.drawable.like),
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
                            modifier = Modifier.height(35.dp).weight(1f).testTag(TestTag.TAG_BUTTON_COMMENT)){
                            Image(
                                painter = painterResource(id = R.drawable.comment),
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
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_YES)
                            ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog.value = false },
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_NO)) {
                            Text("No")
                        }
                    }
                )
            }
        }

        @Composable
        fun BottomNavigationBar(navController: NavHostController, homeViewModel: HomeViewModel) {
            val items = listOf(
                Screen.Home,
                Screen.Friend,
                Screen.Notification,
                Screen.Settings
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color.White)
            ){
                NavigationBar(containerColor = Color.White) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
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
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                        .clickable { }
                ) {
                    FloatingActionButton(
                        onClick = {},
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
        fun BackAndMoreOptionsRow(navController : NavHostController) {
            Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth().padding(10.dp)){
                IconButton(
                    onClick = {
                    // Handle back button click
                    navController.popBackStack()
                },
                    modifier = Modifier.testTag(TestTag.TAG_BUTTON_BACK)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { /* Handle click */ },
                    modifier = Modifier.testTag(TestTag.TAG_BUTTON_MOREOPTIONS)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz, //Horizontal three dots ⋯
                        contentDescription = "More Options",
                        tint = Color.Black
                    )
                }
            }
        }

        @Composable
        fun TabLayout(tabTitles : List<String>,
                      homeViewModel: HomeViewModel,
                      searchViewModel: SearchViewModel,
                      friendViewModel: FriendViewModel = viewModel(),
                      context: Context,
                      onNavigateToShowImageScreen: (image: String) -> Unit,
                      onNavigateToUserInformation: (user: UserInstance) -> Unit){
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
                        Log.d("selectedTabIndex", "People")
                        // Filtered List
                        LazyColumn {
                            val filterList = homeViewModel.listUsers.filter { user ->
                                user.name.contains(searchViewModel.query, ignoreCase = true)
                            }
                            items(filterList){user ->
                                UserRow(user, context, onNavigateToUserInformation)
                            }
                        }
                    }
                    1 -> {
                        Log.d("selectedTabIndex", "Posts")
                        if(searchViewModel.query.isNotEmpty()) {
                            LazyColumn {
                                val filterList = homeViewModel.listNews.filter { news ->
                                    news.message.contains(searchViewModel.query, ignoreCase = true)
                                }
                                items(filterList){new ->
                                    NewsCard(new, context, onNavigateToShowImageScreen, onNavigateToUserInformation, homeViewModel)
                                }
                            }
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
        fun UserRow(user : UserInstance, context : Context, onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToUserInformation(user)
                    }
                    .testTag(TestTag.TAG_FRIEND)){
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(user.image))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(10.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = user.name,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 5.dp) // Adds padding around text
                )
            }
        }

        @Composable
        fun FriendRequest(requester : UserInstance, currentUser : UserInstance, context : Context, onNavigateToUserInformation: (user: UserInstance) -> Unit, friendViewModel: FriendViewModel) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToUserInformation(requester)
                    }
                    .testTag(TestTag.TAG_FRIEND_REQUEST)){
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(requester.image))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(10.dp)
                        .clip(CircleShape)
                )
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
        fun ShowAlertDialogToLogout(context : Context, homeViewModel: HomeViewModel, onNavigateToSignIn:() -> Unit, showDialog : MutableState<Boolean>) {
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
    }
}