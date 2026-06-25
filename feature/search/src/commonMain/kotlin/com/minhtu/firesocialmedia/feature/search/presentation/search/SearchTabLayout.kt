package com.minhtu.firesocialmedia.feature.search.presentation.search

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.utils.UiUtils

@Composable
fun SearchTabLayout(
    listState: LazyListState,
    tabTitles: List<String>,
    localImageLoaderValue: ProvidedValue<*>,
    homeViewModel: HomeViewModelContract,
    searchQuery: String,
    onNavigateToShowImageScreen: (image: String) -> Unit,
    onNavigateToUserInformation: (user: UserInstance?) -> Unit,
    onNavigateToUploadNewsfeed: (updateNew: NewsInstance?) -> Unit
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
                            searchList = homeViewModel.searchUserByName(searchQuery)
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
                        val filterList by remember {
                            derivedStateOf {
                                homeViewModel.listNews.filter { news ->
                                    news.message.contains(searchQuery, ignoreCase = true)
                                }
                            }
                        }
                        UiUtils.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                            localImageLoaderValue = localImageLoaderValue,
                            listState = listState,
                            homeViewModel = homeViewModel,
                            list = filterList,
                            onNavigateToUploadNews = onNavigateToUploadNewsfeed,
                            onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                            onNavigateToUserInformation = onNavigateToUserInformation,
                            showBottomSheet = { news ->
                                newToBeShared = news
                                showBottomSheet = true
                            }
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

