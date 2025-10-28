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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
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
                       onNavigateToCallingScreenWithUI : suspend () -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val commentStatus by homeViewModel.commentStatus.collectAsState()

            val showDialog = remember { mutableStateOf(false) }
            UiUtils.Companion.ShowAlertDialogToLogout(
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

            // Recreate list state exactly once when content first becomes available
            val hasInitialContent = remember { derivedStateOf { newsList.value.isNotEmpty() } }
            val listState = remember(hasInitialContent.value) { LazyListState(0, 0) }
            var didInitialScroll by remember { mutableStateOf(false) }
            Box(modifier = Modifier.Companion
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = modifier.padding(paddingValues)
                ) {
                    //App name and buttons
                    Row(
                        horizontalArrangement = Arrangement.Start, modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "FireSocialMedia",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Companion.Center,
                        )
                        Spacer(modifier = Modifier.Companion.weight(1f))
                        Box(
                            modifier = Modifier.Companion
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    onNavigateToSearch()
                                },
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            CrossPlatformIcon(
                                icon = "search",
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer.toHex(),
                                contentDescription = "Search Icon",
                                contentScale = ContentScale.Companion.Fit,
                                modifier = Modifier.Companion
                                    .size(22.dp)
                                    .testTag(TestTag.Companion.TAG_ICON_BUTTON_SEARCH)
                                    .semantics {
                                        contentDescription =
                                            TestTag.Companion.TAG_ICON_BUTTON_SEARCH
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.Companion.width(15.dp))

                        Box(
                            contentAlignment = Alignment.Companion.Center,
                            modifier = Modifier.Companion
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable {
                                    showDialog.value = true
                                }
                        ) {
                            CrossPlatformIcon(
                                icon = "logout",
                                backgroundColor = MaterialTheme.colorScheme.errorContainer.toHex(),
                                contentDescription = "Logout Icon",
                                contentScale = ContentScale.Companion.Fit,
                                modifier = Modifier.Companion
                                    .size(22.dp)
                                    .testTag(TestTag.Companion.TAG_ICON_BUTTON_LOGOUT)
                                    .semantics {
                                        contentDescription =
                                            TestTag.Companion.TAG_ICON_BUTTON_LOGOUT
                                    }
                            )
                        }
                    }

                    //Create post bar and other users
                    AnimatedVisibility(visible = isAllUsersVisible) {
                        Column(verticalArrangement = Arrangement.Top) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Companion.CenterVertically,
                                modifier = Modifier.Companion.fillMaxWidth()
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
                                            contentScale = ContentScale.Companion.Crop,
                                            modifier = Modifier.Companion
                                                .size(60.dp)
                                                .padding(vertical = 5.dp)
                                                .padding(start = 10.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    onNavigateToUserInformation(homeViewModel.currentUser)
                                                }
                                                .testTag(TestTag.Companion.TAG_CURRENT_USER)
                                                .semantics {
                                                    contentDescription =
                                                        TestTag.Companion.TAG_CURRENT_USER
                                                }
                                        )
                                    }
                                }

                                //Create post
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    modifier = Modifier.Companion
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .clip(RoundedCornerShape(28.dp))
                                        .clickable { onNavigateToUploadNews(null) }
                                        .testTag(TestTag.Companion.TAG_CREATE_POST)
                                        .semantics { contentDescription = TestTag.Companion.TAG_CREATE_POST },
                                    placeholder = { Text(text = "What are you thinking?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    enabled = false,    // Disables the TextField
                                    singleLine = true,
                                    shape = RoundedCornerShape(28.dp)
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .testTag(TestTag.Companion.TAG_USERS_ROW)
                                    .semantics {
                                        contentDescription = TestTag.Companion.TAG_USERS_ROW
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
                                if(!didInitialScroll && totalItems > 0 && !userInteracted) {
                                    listState.scrollToItem(0, 0)
                                    didInitialScroll = true
                                }
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
                            derivedStateOf { newsList.value.sortedByDescending { it.timePosted } }
                        }
                        UiUtils.Companion.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                            localImageLoaderValue,
                            listState,
                            homeViewModel,
                            sortedNews,
                            onNavigateToUploadNews,
                            onNavigateToShowImageScreen,
                            onNavigateToUserInformation
                        )
                    }
                }
                ScrollToTopButton(listState, Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 80.dp))
                if (isLoading) {
                    Loading.Companion.LoadingScreen()
                }
            }
        }

        @Composable
        private fun UserCard(user: UserInstance,
                             localImageLoaderValue : ProvidedValue<*>,
                             onNavigateToUserInformation: (user: UserInstance) -> Unit) {
            Card(
                modifier = Modifier.Companion.size(70.dp, 90.dp)
                    .testTag(TestTag.Companion.TAG_ITEM_IN_ROW)
                    .semantics {
                        contentDescription = TestTag.Companion.TAG_ITEM_IN_ROW
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                    modifier = Modifier.Companion.fillMaxSize()
                ) {
                    CompositionLocalProvider(
                        localImageLoaderValue
                    ) {
                        AutoSizeImage(
                            user.image,
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Companion.Crop,
                            modifier = Modifier.Companion
                                .weight(1f) // Allocates equal space to the image and text
                                .clip(CircleShape)
                                .clickable {
                                    // Handle image click
                                    onNavigateToUserInformation(user)
                                }
                        )
                    }
                    Spacer(modifier = Modifier.Companion.height(1.dp)) // Optional spacing between image and text
                    Text(
                        text = user.name,
                        color = Color.Companion.Black,
                        maxLines = 1,
                        textAlign = TextAlign.Companion.Center,
                        overflow = TextOverflow.Companion.Ellipsis, // Adds "..." at the end if the text overflows
                        modifier = Modifier.Companion.padding(horizontal = 4.dp) // Adds padding around text
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