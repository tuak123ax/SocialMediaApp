package com.minhtu.firesocialmedia.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.UiUtils

class Search {
    companion object{
        @Composable
        fun SearchScreen(modifier: Modifier,
                         searchViewModel: SearchViewModel,
                         homeViewModel: HomeViewModel,
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
            Column(verticalArrangement = Arrangement.Top, modifier = modifier) {
                UiUtils.BackAndMoreOptionsRow(onNavigateBack)
                SearchBar(
                    query = searchViewModel.query,
                    onQueryChange = { query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier
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
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                placeholder = { Text(text = placeholder) },
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}