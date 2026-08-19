package com.minhtu.firesocialmedia.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.minhtu.firesocialmedia.constants.search.TestTag
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance as CoreNewsInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.search.SessionViewModel
import org.koin.compose.koinInject
import com.minhtu.firesocialmedia.search.entity.news.NewsInstance
import com.minhtu.firesocialmedia.search.entity.news.toCoreNews
import com.minhtu.firesocialmedia.search.entity.news.toSearchNews
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.utils.UiUtils
import com.minhtu.firesocialmedia.utils.search.FeedListUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchTabLayout(
    listState: LazyListState,
    tabTitles: List<String>,
    localImageLoaderValue: ProvidedValue<*>,
    homeViewModel: HomeViewModel,
    sessionViewModel: SessionViewModel = koinInject(),
    searchQuery: String,
    onNavigateToShowImageScreen: (image: String) -> Unit,
    onNavigateToUserInformation: (user: UserInstance?) -> Unit,
    onNavigateToUploadNewsfeed: (updateNew: CoreNewsInstance?) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var newToBeShared by mutableStateOf<NewsInstance?>(null)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selectedTabIndex == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    if (searchQuery.isNotEmpty()) {
                        var searchList by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
                        LaunchedEffect(searchQuery) {
                            searchList = sessionViewModel.searchUserByName(searchQuery)
                        }
                        LazyColumn(
                            modifier = Modifier
                                .testTag(TestTag.TAG_PEOPLE_COLUMN)
                                .semantics {
                                    contentDescription = TestTag.TAG_PEOPLE_COLUMN
                                }
                        ) {
                            items(searchList) { user ->
                                UiUtils.SearchUserCard(
                                    user,
                                    localImageLoaderValue,
                                    onClickViewProfileButton = {
                                        onNavigateToUserInformation(user)
                                    }
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Please input person you want to search"
                            )
                        }
                    }
                }

                1 -> {
                    if (searchQuery.isNotEmpty()) {
                        LaunchedEffect(searchQuery) {
                            sessionViewModel.searchNews(searchQuery)
                        }
                        val filterList = sessionViewModel.newsSearchResults.map { it.toSearchNews() }

                        LaunchedEffect(listState) {
                            snapshotFlow {
                                val layoutInfo = listState.layoutInfo
                                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                val totalItems = layoutInfo.totalItemsCount
                                lastVisible to totalItems
                            }
                                .distinctUntilChanged()
                                .collectLatest { (lastVisible, totalItems) ->
                                    if (totalItems > 0 &&
                                        lastVisible >= totalItems - 3 &&
                                        !sessionViewModel.isLoadingMoreNews &&
                                        sessionViewModel.hasMoreNews
                                    ) {
                                        sessionViewModel.loadMoreMatchingNews()
                                    }
                                }
                        }

                        FeedListUtils.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                            localImageLoaderValue = localImageLoaderValue,
                            listState = listState,
                            homeViewModel = homeViewModel,
                            sessionViewModel = sessionViewModel,
                            list = filterList,
                            onNavigateToUploadNews = { updateNew -> onNavigateToUploadNewsfeed(updateNew?.toCoreNews()) },
                            onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                            onNavigateToUserInformation = onNavigateToUserInformation,
                            showBottomSheet = { news ->
                                newToBeShared = news
                                showBottomSheet = true
                            },
                            isLoadingMore = sessionViewModel.isLoadingMoreNews
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Please input content you want to search"
                            )
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            UiUtils.ShareBottomSheet(
                deepLink = "https://firechat-aa433.web.app/news/${newToBeShared?.id}",
                onDismiss = {
                    showBottomSheet = false
                },
                onClick = {
                    showBottomSheet = false
                }
            )
        }
    }
}

