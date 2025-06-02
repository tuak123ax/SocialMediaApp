package com.minhtu.firesocialmedia.home.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.UiUtils

class Search {
    companion object{
        @Composable
        fun SearchScreen(modifier: Modifier,
                         platform : PlatformContext,
                         searchViewModel: SearchViewModel,
                         homeViewModel: HomeViewModel,
                         navController: NavigationHandler,
                         onNavigateToUserInformation: (user : UserInstance?) -> Unit,
                         onNavigateToShowImageScreen: (image : String) -> Unit,
                         onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
                         onNavigateToUploadNewsFeed : (updateNew : NewsInstance?) -> Unit){
            val commentStatus by homeViewModel.commentStatus.collectAsState()
            LaunchedEffect(commentStatus){
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }
            Column(verticalArrangement = Arrangement.Top, modifier = modifier) {
                UiUtils.BackAndMoreOptionsRow(navController)
                SearchBar(query = searchViewModel.query, onQueryChange = {
                    query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier
                        .testTag(TestTag.TAG_SEARCH_BAR)
                        .semantics{
                            contentDescription = TestTag.TAG_SEARCH_BAR
                        }
                )

                UiUtils.TabLayout(
                    platform,
                    listOf("People","Posts"),
                    homeViewModel,
                    searchViewModel,
                    onNavigateToShowImageScreen,
                    onNavigateToUserInformation,
                    onNavigateToUploadNewsFeed)
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
                modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
                placeholder = {Text(text = placeholder)},
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                singleLine = true,
                shape = RoundedCornerShape(30.dp)
            )
        }
    }
}