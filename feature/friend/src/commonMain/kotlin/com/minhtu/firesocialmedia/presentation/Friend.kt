package com.minhtu.firesocialmedia.presentation.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.friend.TestTag
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.presentation.SessionViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.koin.compose.koinInject

class Friend {
    companion object {
        @Composable
        fun FriendScreen(
            modifier: Modifier,
            paddingValues: PaddingValues,
            localImageLoaderValue: ProvidedValue<*>,
            sessionViewModel: SessionViewModel = koinInject(),
            friendViewModel: FriendViewModel = koinInject(),
            onNavigateToUserInformation: (user: UserInstance) -> Unit
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                val coroutineScope = rememberCoroutineScope()
                var searchQuery by remember { mutableStateOf("") }
                val friendRequestsStatus = friendViewModel.friendRequestsStatus.collectAsState().value
                val friendStatus = friendViewModel.friendStatus.collectAsState().value
                val currentUser = sessionViewModel.currentUserState.collectAsState().value

                LaunchedEffect(currentUser) {
                    val user = currentUser ?: return@LaunchedEffect
                    friendViewModel.updateFriendRequests(user.friendRequests)
                    friendViewModel.updateFriends(user.friends)
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
                FriendSearchBar(
                    query = searchQuery,
                    onQueryChange = { query -> searchQuery = query },
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .testTag(TestTag.TAG_SEARCH_BAR)
                        .semantics { contentDescription = TestTag.TAG_SEARCH_BAR }
                )
                TabLayoutForFriendScreen(
                    tabTitles = listOf("Friends", "Requests"),
                    localImageLoaderValue = localImageLoaderValue,
                    sessionViewModel = sessionViewModel,
                    searchQuery = searchQuery,
                    friendViewModel = friendViewModel,
                    friendRequestsStatus = friendRequestsStatus,
                    friendStatus = friendStatus,
                    onNavigateToUserInformation = onNavigateToUserInformation,
                    coroutineScope = coroutineScope
                )
            }
        }

        @Composable
        private fun FriendSearchBar(
            query: String,
            onQueryChange: (String) -> Unit,
            modifier: Modifier,
            placeholder: String = "Search..."
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        @Composable
        private fun TabLayoutForFriendScreen(
            tabTitles: List<String>,
            localImageLoaderValue: ProvidedValue<*>,
            sessionViewModel: SessionViewModel,
            searchQuery: String,
            friendViewModel: FriendViewModel,
            friendRequestsStatus: List<String>,
            friendStatus: List<String>,
            onNavigateToUserInformation: (user: UserInstance) -> Unit,
            coroutineScope: kotlinx.coroutines.CoroutineScope
        ) {
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            var filteredFriends by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
            var filteredRequests by remember { mutableStateOf<List<UserInstance>>(emptyList()) }

            LaunchedEffect(friendStatus, searchQuery) {
                filteredFriends = friendStatus.map { userId ->
                    async {
                        sessionViewModel.findUserById(userId)?.takeIf {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }.awaitAll().filterNotNull().distinct()
            }

            LaunchedEffect(friendRequestsStatus, searchQuery) {
                filteredRequests = friendRequestsStatus.map { userId ->
                    async {
                        sessionViewModel.findUserById(userId)?.takeIf {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }.awaitAll().filterNotNull().distinct()
            }

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
                        val count = if (index == 0) filteredFriends.size else filteredRequests.size

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
                                        color = if (selectedTabIndex == index) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
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

                when (selectedTabIndex) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .testTag(TestTag.TAG_FRIEND_TAB_LIST)
                                .semantics { contentDescription = TestTag.TAG_FRIEND_TAB_LIST }
                        ) {
                            items(filteredFriends) { user ->
                                UiUtils.SearchUserCard(
                                    user = user,
                                    localImageLoaderValue = localImageLoaderValue,
                                    onClickViewProfileButton = { onNavigateToUserInformation(user) }
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .testTag(TestTag.TAG_FRIEND_REQUEST_TAB_LIST)
                                .semantics { contentDescription = TestTag.TAG_FRIEND_REQUEST_TAB_LIST }
                        ) {
                            items(filteredRequests) { user ->
                                FriendRequestCard(
                                    localImageLoaderValue = localImageLoaderValue,
                                    requester = user,
                                    onNavigateToUserInformation = onNavigateToUserInformation,
                                    onAccept = {
                                        sessionViewModel.currentUser?.let { currentUser ->
                                            friendViewModel.acceptFriendRequest(user, currentUser)
                                        }
                                    },
                                    onReject = {
                                        sessionViewModel.currentUser?.let { currentUser ->
                                            friendViewModel.rejectFriendRequest(user, currentUser)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
