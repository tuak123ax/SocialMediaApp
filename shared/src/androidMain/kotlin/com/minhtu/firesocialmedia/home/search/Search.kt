package com.minhtu.firesocialmedia.home.search

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.UiUtils

class Search {
    companion object{
        @Composable
        fun SearchScreen(modifier: Modifier,
                         searchViewModel: SearchViewModel,
                         homeViewModel: HomeViewModel,
                         onNavigateToUserInformation: (user : UserInstance) -> Unit,
                         onNavigateToShowImageScreen: (image : String) -> Unit,
                         onNavigateToHomeScreen:() -> Unit,
                         onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit){
            val lifecycleOwner = LocalLifecycleOwner.current
            val commentStatus by homeViewModel.commentStatus.collectAsStateWithLifecycle(
                initialValue = null,
                lifecycleOwner = lifecycleOwner
            )
            LaunchedEffect(commentStatus){
                commentStatus?.let { selectedNew ->
                    Log.e("HomeScreen", "selected new: $selectedNew")
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }
            Column(verticalArrangement = Arrangement.Top, modifier = modifier) {
                val context = LocalContext.current
                UiUtils.BackAndMoreOptionsRow(onNavigateToHomeScreen)
                SearchBar(query = searchViewModel.query, onQueryChange = {
                    query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier
                )

                UiUtils.TabLayout(listOf("People","Posts"),
                    Search.getScreenName(),
                    homeViewModel,
                    searchViewModel,
                    context,
                    onNavigateToShowImageScreen,
                    onNavigateToUserInformation)
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