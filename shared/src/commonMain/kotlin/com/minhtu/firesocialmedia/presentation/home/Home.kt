package com.minhtu.firesocialmedia.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.core.DecentralizationType
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.home.deeplinks.DeepLinksData
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.sharedmodule.ui.theme.homeEditTextBackgroundColor
import com.minhtu.sharedmodule.ui.theme.iconButtonBackgroundColor
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class Home {
    companion object{
        @Composable
        fun HomeScreen(modifier: Modifier,
                       homeViewModel: HomeViewModel,
                       loadingViewModel: LoadingViewModel,
                       navigateToCallingScreen : Boolean,
                       paddingValues: PaddingValues,
                       localImageLoaderValue : ProvidedValue<*>,
                       onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
                       onNavigateToShowImageScreen: (image : String) -> Unit,
                       onNavigateToSearch: () -> Unit,
                       onNavigateToSignIn: () -> Unit,
                       onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                       onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
                       onNavigateToCallingScreen : suspend (CallingRequestData) -> Unit,
                       onNavigateToCallingScreenWithUI : suspend () -> Unit,
                       onNavigateToPostInformation : () -> Unit,
                       onShareNews : (String, NewsInstance) -> Unit,
                       onNavigateToJoinGroup : () -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val commentStatus by homeViewModel.commentStatus.collectAsState()
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by remember { mutableStateOf<NewsInstance?>(null) }
            val showDialog = remember { mutableStateOf(false) }
            UiUtils.ShowAlertDialogToLogout(
                onClickConfirm = {
                    homeViewModel.clearAccountInStorage()
                    homeViewModel.clearLocalData()
                },
                onNavigateToSignIn,
                showDialog
            )

            //Observe Live Data as State
            val usersList by  homeViewModel.allUserFriends.collectAsState()

            val numberOfLists by remember { derivedStateOf { homeViewModel.numberOfListNeedToLoad } }

            val newsList = homeViewModel.allNews.collectAsStateWithLifecycle()

            val currentUserState = homeViewModel.currentUserState

            var isAllUsersVisible by remember { mutableStateOf(true) }
            var userInteracted by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                loadingViewModel.showLoading()
                //Load users list and news list.
                homeViewModel.getCurrentUserAndFriends()
                homeViewModel.getLatestNews()
                homeViewModel.getAllNotificationsOfUser()
                homeViewModel.decreaseNumberOfListNeedToLoad(1)
                if (numberOfLists == 0) {
                    loadingViewModel.hideLoading()
                }
                //Check deeplink after loading necessary data
                if(DeepLinksData.deepLink.isNotEmpty()) {
                    if(DeepLinksData.deepLink.contains("news")) {
                        onNavigateToPostInformation()
                    } else if (DeepLinksData.deepLink.contains("groups")) {
                        onNavigateToJoinGroup()
                    }
                }
            }
            LaunchedEffect(newsList.value) {
                homeViewModel.decreaseNumberOfListNeedToLoad(1)
                if (numberOfLists == 0) {
                    loadingViewModel.hideLoading()
                }
            }
            LaunchedEffect(commentStatus) {
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }

            val getCurrentUserStatus by homeViewModel.getCurrentUserStatus

            LaunchedEffect(getCurrentUserStatus) {
                if(getCurrentUserStatus) {
                    logMessage("observePhoneCall", { "start observe phone call" })
                    homeViewModel.observePhoneCall()
                    if(navigateToCallingScreen) {
                        logMessage("navigateToCallingScreen", { "onNavigateToCallingScreenWithUI" })
                        onNavigateToCallingScreenWithUI()
                    }
                }
            }
            LaunchedEffect(Unit) {
                if(navigateToCallingScreen) {
                    logMessage("navigateToCallingScreen", { "onNavigateToCallingScreenWithUI" })
                    onNavigateToCallingScreenWithUI()
                }
            }

            val phoneCallRequestStatus by homeViewModel.phoneCallRequestStatus.collectAsState()
            LaunchedEffect(phoneCallRequestStatus) {
                if(phoneCallRequestStatus != null) {
                    onNavigateToCallingScreen(phoneCallRequestStatus!!)
                }
            }

            //Observe share post status
            val sharePostStatus by homeViewModel.sharePostStatus.collectAsState()
            val sharePostError by homeViewModel.shareError.collectAsState()
            LaunchedEffect(sharePostStatus) {
                //Share post result
                if(sharePostStatus != null) {
                    if(sharePostStatus!!) {
                        showToast("Share successfully!!!")
                    } else {
                        showToast("Error happened. Please try again!!!")
                    }
                    homeViewModel.resetShareContentAndStatus()
                }
            }
            LaunchedEffect(sharePostError) {
                //Error when share post
                if(sharePostError != null) {
                    showToast("Cannot get content to share. Please try again!!!")
                }
                homeViewModel.resetShareContentAndStatus()
            }

            // Preserve scroll position across navigation/back stack using rememberSaveable
            val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState(0, 0) }
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = modifier.padding(paddingValues)
                ) {
                    //App name and buttons
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "FireSocialMedia",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconButtonBackgroundColor)
                                .clickable {
                                    onNavigateToSearch()
                                }
                                .testTag(TestTag.TAG_ICON_BUTTON_SEARCH)
                                .semantics {
                                    contentDescription =
                                        TestTag.TAG_ICON_BUTTON_SEARCH
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CrossPlatformIcon(
                                icon = "search",
                                backgroundColor = iconButtonBackgroundColor.toHex(),
                                contentDescription = "Search Icon",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(15.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconButtonBackgroundColor)
                                .clickable {
                                    showDialog.value = true
                                }
                                .testTag(TestTag.TAG_ICON_BUTTON_LOGOUT)
                                .semantics {
                                    contentDescription =
                                        TestTag.TAG_ICON_BUTTON_LOGOUT
                                }
                        ) {
                            CrossPlatformIcon(
                                icon = "logout",
                                backgroundColor = iconButtonBackgroundColor.toHex(),
                                contentDescription = "Logout Icon",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(4.dp)
                            )
                        }
                    }

                    //Create post bar and other users
                    AnimatedVisibility(visible = isAllUsersVisible) {
                        Column(verticalArrangement = Arrangement.Top) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                //Current user avatar
                                if (currentUserState != null) {
                                    val userImage = currentUserState.image // Avoid force unwrapping

                                    CompositionLocalProvider(
                                        localImageLoaderValue
                                    ) {
                                        AutoSizeImage(
                                            userImage,
                                            contentDescription = "Poster Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(vertical = 5.dp)
                                                .padding(start = 10.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    onNavigateToUserInformation(homeViewModel.currentUser)
                                                }
                                                .testTag(TestTag.TAG_CURRENT_USER)
                                                .semantics {
                                                    contentDescription =
                                                        TestTag.TAG_CURRENT_USER
                                                }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                //Create post
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(homeEditTextBackgroundColor)
                                        .clickable { onNavigateToUploadNews(null) }
                                        .padding(horizontal = 16.dp)
                                        .testTag(TestTag.TAG_CREATE_POST)
                                        .semantics { contentDescription = TestTag.TAG_CREATE_POST },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "What are you thinking?",
                                        fontSize = 14.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .testTag(TestTag.TAG_USERS_ROW)
                                    .semantics {
                                        contentDescription = TestTag.TAG_USERS_ROW
                                    }
                            ) {
                                usersList.forEach { user ->
                                    if(user != null) {
                                        item {
                                            UserCard(
                                                user = user,
                                                localImageLoaderValue,
                                                onNavigateToUserInformation = onNavigateToUserInformation
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // LaunchedEffect to track the scroll state (hide top bar and show load more)
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val layoutInfo = listState.layoutInfo
                            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val totalItems = layoutInfo.totalItemsCount
                            val inProgress = listState.isScrollInProgress

                            Triple(firstVisible, lastVisible, totalItems) to inProgress
                        }
                            .distinctUntilChanged()
                            .collectLatest { (triple, state) ->
                                val (firstVisible, lastVisible, totalItems) = triple
                                val inProgress = state
                                if(inProgress && firstVisible > 0) {
                                    userInteracted = true
                                }
                                // Show/hide top bar
                                isAllUsersVisible = if(!userInteracted) {
                                    true
                                } else {
                                    firstVisible == 0
                                }

                                // Trigger load more when near bottom
                                if (
                                    firstVisible > 0 &&
                                    lastVisible >= totalItems - 3 &&
                                    !homeViewModel.isLoadingMore.value &&
                                    homeViewModel.hasMoreData.value
                                ) {
                                    homeViewModel.loadMoreNews()
                                }
                            }
                    }

                    //Newsfeed
                    PullToRefreshLayout(
                        isRefreshing = homeViewModel.isRefreshing.value,
                        onRefresh = {
                            homeViewModel.refreshNews()
                        },
                        canRefresh = {listState.isAtTop()}
                    ) {
                        val sortedNews by remember(newsList.value) {
                            derivedStateOf {
                                newsList.value
                                    .asSequence()
                                    .filter { item ->
                                        when (item.decentralizationType) {
                                            DecentralizationType.Public -> true
                                            DecentralizationType.OnlyFriends -> homeViewModel.isFriendOf(item.posterId)
                                            DecentralizationType.Private -> false
                                            null -> true
                                        }
                                    }
                                    .sortedByDescending { it.timePosted }
                                    .toList()
                            }
                        }

                        UiUtils.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                            localImageLoaderValue,
                            listState,
                            homeViewModel,
                            sortedNews,
                            onNavigateToUploadNews,
                            onNavigateToShowImageScreen,
                            onNavigateToUserInformation,
                            showBottomSheet = { news ->
                                newToBeShared = news
                                showBottomSheet = true
                            }
                        )
                    }
                }
                ScrollToTopButton(listState, Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 80.dp))
                if(showBottomSheet) {
                    UiUtils.ShareBottomSheet(
                        deepLink = "https://firechat-aa433.web.app/news/${newToBeShared?.id}",
                        onDismiss = {
                            showBottomSheet = false
                        },
                        onClick = { message ->
                            showBottomSheet = false
                            //Continue with share process
                            if(newToBeShared != null) {
                                onShareNews(message, newToBeShared!!)
                            } else {
                                showToast("Cannot share now. Please try again!!!")
                            }
                        }
                    )
                }
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        private fun UserCard(user: UserInstance,
                             localImageLoaderValue : ProvidedValue<*>,
                             onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Card(
                modifier = Modifier.size(70.dp, 90.dp)
                    .testTag(TestTag.TAG_ITEM_IN_ROW)
                    .semantics {
                        contentDescription = TestTag.TAG_ITEM_IN_ROW
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CompositionLocalProvider(
                        localImageLoaderValue
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

        @Composable
        private fun ScrollToTopButton(listState : LazyListState, modifier: Modifier) {
            val scope = rememberCoroutineScope()
            val showTopButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 5 } }

            AnimatedVisibility(visible = showTopButton, modifier = modifier){
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .testTag(TestTag.SCROLL_TO_TOP_BUTTON)
                        .semantics{
                            contentDescription = TestTag.SCROLL_TO_TOP_BUTTON
                        },
                    containerColor = Color.White
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Scroll to top")
                }
            }
        }

        @Composable
        fun PullToRefreshLayout(
            isRefreshing: Boolean,
            onRefresh: () -> Unit,
            canRefresh: () -> Boolean,
            refreshThresholdDp: Float = 72f,
            indicator: @Composable (progress: Float, refreshing: Boolean) -> Unit = { p, r ->
                DefaultRefreshIndicator(progress = p, refreshing = r)
            },
            content: @Composable () -> Unit
        ) {
            val density = LocalDensity.current
            val thresholdPx = remember(density, refreshThresholdDp) { with(density) { refreshThresholdDp.dp.toPx() } }

            // offset in PX; mutate directly on drag (no coroutine per event)
            var offset by remember { mutableFloatStateOf(0f) }

            // keep latest lambdas without causing extra recompositions
            val updOnRefresh by rememberUpdatedState(onRefresh)
            val updCanRefresh by rememberUpdatedState(canRefresh)

            // Animate only on state changes (start/stop refreshing)
            LaunchedEffect(isRefreshing, thresholdPx) {
                val start = offset
                val target = if (isRefreshing) thresholdPx else 0f
                if (start != target) {
                    androidx.compose.animation.core.animate(
                        initialValue = start,
                        targetValue = target,
                        animationSpec = tween(durationMillis = if (isRefreshing) 120 else 160)
                    ) { value, _ -> offset = value }
                }
            }

            val connection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        // pull down while at top -> consume and move indicator
                        if (source == NestedScrollSource.Drag && available.y > 0f && updCanRefresh()) {
                            val new = (offset + available.y * 0.5f).coerceAtLeast(0f)
                            offset = new
                            return Offset(0f, available.y) // consumed
                        }
                        return Offset.Zero
                    }

                    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                        // push up -> reduce offset
                        if (source == NestedScrollSource.Drag && available.y < 0f && offset > 0f) {
                            offset = maxOf(0f, offset + available.y)
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        // on release
                        if (offset >= thresholdPx && !isRefreshing) {
                            updOnRefresh()
                        } else {
                            // snap back
                            val start = offset
                            if (start != 0f) {
                                androidx.compose.animation.core.animate(
                                    initialValue = start,
                                    targetValue = 0f,
                                    animationSpec = tween(160)
                                ) { value, _ -> offset = value }
                            }
                        }
                        return Velocity.Zero
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(connection)
            ) {
                content()

                // Progress derived from offset
                val progress by remember { derivedStateOf { (offset / thresholdPx).coerceIn(0f, 1f) } }

                // Only show when pulling or actively refreshing
                val showIndicator by remember { derivedStateOf { isRefreshing || offset > 0.5f } }

                if (showIndicator) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset { IntOffset(0, offset.toInt()) },
                        contentAlignment = Alignment.Center
                    ) {
                        indicator(progress, isRefreshing)
                    }
                }
            }
        }


        @Composable
        private fun DefaultRefreshIndicator(progress: Float, refreshing: Boolean) {
            if (!refreshing && progress <= 0f) return

            val size = (32 + 16 * progress).dp
            val bg = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (isSystemInDarkTheme()) 0.18f else 0.08f
            )
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(size)
                    .background(bg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (refreshing) {
                    CircularProgressIndicator(strokeWidth = 2.5.dp)
                } else {
                    CircularProgressIndicator(progress = progress, strokeWidth = 2.5.dp)
                }
            }
        }

        fun LazyListState.isAtTop(): Boolean =
            firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0

        fun getScreenName(): String{
            return "HomeScreen"
        }
    }

}