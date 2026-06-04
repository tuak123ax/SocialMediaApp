package com.minhtu.firesocialmedia.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.utils.UiUtils
import org.koin.compose.viewmodel.koinViewModel

class Search {
    companion object{
        @Composable
        fun SearchScreen(modifier: Modifier,
                         paddingValues: PaddingValues,
                         searchViewModel: SearchViewModel = koinViewModel(),
                         homeViewModel: HomeViewModelContract,
                         localImageLoaderValue : ProvidedValue<*>,
                         onNavigateBack: () -> Unit,
                         onNavigateToUserInformation: (user : UserInstance?) -> Unit,
                         onNavigateToShowImageScreen: (image : String) -> Unit,
                         onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
                         onNavigateToUploadNewsFeed : (updateNew : NewsInstance?) -> Unit){
            val commentStatus by homeViewModel.commentStatus.collectAsState()
            val listState = rememberLazyListState()
            LaunchedEffect(commentStatus) {
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }
            Column(verticalArrangement = Arrangement.Top,
                modifier = modifier
                    .padding(paddingValues)) {
                UiUtils.BackAndTitleAndMoreOptionsRow(
                    title = "Search",
                    trailingIcon = "more_horiz",
                    isMember = false,
                    navigateBack = {
                        onNavigateBack()
                    }
                )
                SearchBar(
                    query = searchViewModel.query,
                    onQueryChange = { query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .testTag(TestTag.TAG_SEARCH_BAR)
                        .semantics {
                            contentDescription = TestTag.TAG_SEARCH_BAR
                        }
                )

                UiUtils.TabLayout(
                    listState,
                    listOf("People", "Posts"),
                    localImageLoaderValue,
                    homeViewModel,
                    searchViewModel,
                    onNavigateToShowImageScreen,
                    onNavigateToUserInformation,
                    onNavigateToUploadNewsFeed
                )
            }
        }

        fun getScreenName() : String {
            return "SearchScreen"
        }
        @Composable
        fun SearchBar(
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
    }
}