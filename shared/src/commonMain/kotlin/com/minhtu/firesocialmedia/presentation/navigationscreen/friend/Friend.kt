package com.minhtu.firesocialmedia.presentation.navigationscreen.friend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.Utils

class Friend {
    companion object{
        @Composable
        fun FriendScreen(platform : PlatformContext,
                         modifier: Modifier,
                         paddingValues: PaddingValues,
                         searchViewModel: SearchViewModel,
                         homeViewModel: HomeViewModel,
                         friendViewModel: FriendViewModel,
                         onNavigateToUserInformation: (user : UserInstance) -> Unit,
                         onNavigateToShowImageScreen: (image : String) -> Unit){
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                val friendRequestsStatus =
                    friendViewModel.friendRequestsStatus.collectAsState().value
                val friendStatus = friendViewModel.friendStatus.collectAsState().value

                LaunchedEffect(Unit) {
                    friendViewModel.updateFriendRequests(homeViewModel.currentUser!!.friendRequests)
                    friendViewModel.updateFriends(homeViewModel.currentUser!!.friends)
                }
                Search.Companion.SearchBar(
                    query = searchViewModel.query,
                    onQueryChange = { query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier.Companion.height(80.dp).padding(vertical = 10.dp)
                        .testTag(TestTag.Companion.TAG_SEARCH_BAR)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_SEARCH_BAR
                        }
                )
                Text(
                    text = "Friends",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 20.dp)
                )
                TabLayoutForFriendScreen(
                    platform,
                    listOf("Friends", "Friend Requests"),
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
        fun TabLayoutForFriendScreen(platform : PlatformContext,
                                     tabTitles : List<String>,
                                     homeViewModel: HomeViewModel,
                                     searchViewModel: SearchViewModel,
                                     friendViewModel: FriendViewModel,
                                     friendRequestsStatus: List<String>, friendStatus: List<String>,
                                     onNavigateToUserInformation: (user: UserInstance) -> Unit){
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            Column(modifier = Modifier.Companion.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Companion.White,
                    contentColor = Color.Companion.Black,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.Companion.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color.Companion.Red
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
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
                when (selectedTabIndex) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.Companion
                                .testTag(TestTag.Companion.TAG_FRIEND_TAB_LIST)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_FRIEND_TAB_LIST
                                }) {
                            val filterList = friendStatus.filter { userId ->
                                val user =
                                    Utils.Companion.findUserById(userId, homeViewModel.listUsers)
                                user?.name?.contains(
                                    searchViewModel.query,
                                    ignoreCase = true
                                ) == true
                            }
                            items(filterList) { userId ->
                                val user =
                                    Utils.Companion.findUserById(userId, homeViewModel.listUsers)
                                if (user != null) {
                                    UiUtils.Companion.UserRow(user, onNavigateToUserInformation)
                                }
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            modifier = Modifier.Companion
                                .testTag(TestTag.Companion.TAG_FRIEND_REQUEST_TAB_LIST)
                                .semantics {
                                    contentDescription =
                                        TestTag.Companion.TAG_FRIEND_REQUEST_TAB_LIST
                                }) {
                            val filterList = friendRequestsStatus.filter { userId ->
                                val user =
                                    Utils.Companion.findUserById(userId, homeViewModel.listUsers)
                                user?.name?.contains(
                                    searchViewModel.query,
                                    ignoreCase = true
                                ) == true
                            }
                            items(filterList) { userId ->
                                val requester =
                                    Utils.Companion.findUserById(userId, homeViewModel.listUsers)
                                if (requester != null) {
                                    UiUtils.Companion.FriendRequest(
                                        platform,
                                        requester,
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
}