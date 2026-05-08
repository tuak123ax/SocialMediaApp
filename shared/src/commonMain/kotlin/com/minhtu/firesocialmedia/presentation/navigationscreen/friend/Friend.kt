package com.minhtu.firesocialmedia.presentation.navigationscreen.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class Friend {
    companion object{
        @Composable
        fun FriendScreen(modifier: Modifier,
                         paddingValues: PaddingValues,
                         localImageLoaderValue : ProvidedValue<*>,
                         searchViewModel: SearchViewModel,
                         homeViewModel: HomeViewModel,
                         friendViewModel: FriendViewModel,
                         onNavigateToUserInformation: (user : UserInstance) -> Unit,
                         onNavigateToShowImageScreen: (image : String) -> Unit){
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                val friendRequestsStatus =
                    friendViewModel.friendRequestsStatus.collectAsState().value
                val friendStatus = friendViewModel.friendStatus.collectAsState().value

                LaunchedEffect(Unit) {
                    friendViewModel.updateFriendRequests(homeViewModel.currentUser!!.friendRequests)
                    friendViewModel.updateFriends(homeViewModel.currentUser!!.friends)
                }
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
                Search.SearchBar(
                    query = searchViewModel.query,
                    onQueryChange = { query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier.padding(horizontal = 10.dp)
                        .testTag(TestTag.TAG_SEARCH_BAR)
                        .semantics {
                            contentDescription = TestTag.TAG_SEARCH_BAR
                        }
                )
                TabLayoutForFriendScreen(
                    listOf("Friends", "Requests"),
                    localImageLoaderValue,
                    homeViewModel = homeViewModel,
                    searchViewModel = searchViewModel,
                    friendViewModel = friendViewModel,
                    friendRequestsStatus = friendRequestsStatus,
                    friendStatus = friendStatus,
                    onNavigateToUserInformation = onNavigateToUserInformation
                )
            }
        }

        fun getScreenName() : String {
            return "FriendScreen"
        }

        @Composable
        fun TabLayoutForFriendScreen(
            tabTitles: List<String>,
            localImageLoaderValue: ProvidedValue<*>,
            homeViewModel: HomeViewModel,
            searchViewModel: SearchViewModel,
            friendViewModel: FriendViewModel,
            friendRequestsStatus: List<String>,
            friendStatus: List<String>,
            onNavigateToUserInformation: (user: UserInstance) -> Unit
        ) {
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            var filteredFriends by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
            var filteredRequests by remember { mutableStateOf<List<UserInstance>>(emptyList()) }

            // Filter friends
            LaunchedEffect(friendStatus, searchViewModel.query) {
                filteredFriends = friendStatus.map { userId ->
                    async {
                        homeViewModel.findUserById(userId)
                            ?.takeIf {
                                it.name.contains(searchViewModel.query, ignoreCase = true)
                            }
                    }
                }.awaitAll().filterNotNull().distinct()
            }

            // Filter requests
            LaunchedEffect(friendRequestsStatus, searchViewModel.query) {
                filteredRequests = friendRequestsStatus.map { userId ->
                    async {
                        homeViewModel.findUserById(userId)
                            ?.takeIf {
                                it.name.contains(searchViewModel.query, ignoreCase = true)
                            }
                    }
                }.awaitAll().filterNotNull().distinct()
            }

            Column(modifier = Modifier.fillMaxSize()) {

                // TAB ROW
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

                        val count = when (index) {
                            0 -> filteredFriends.size
                            else -> filteredRequests.size
                        }

                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (count > 0) {
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (count <= 999) count.toString() else "999+",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // CONTENT
                when (selectedTabIndex) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .testTag(TestTag.TAG_FRIEND_TAB_LIST)
                                .semantics { contentDescription = TestTag.TAG_FRIEND_TAB_LIST }
                        ) {
                            items(filteredFriends) { user ->
                                UiUtils.SearchUserCard(
                                    user,
                                    localImageLoaderValue,
                                    onClickViewProfileButton = {
                                        onNavigateToUserInformation(user)
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            modifier = Modifier
                                .testTag(TestTag.TAG_FRIEND_REQUEST_TAB_LIST)
                                .semantics { contentDescription = TestTag.TAG_FRIEND_REQUEST_TAB_LIST }
                        ) {
                            items(filteredRequests) { user ->
                                UiUtils.FriendRequest(
                                    localImageLoaderValue,
                                    user,
                                    homeViewModel.currentUser!!,
                                    onNavigateToUserInformation,
                                    friendViewModel
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}