package com.minhtu.firesocialmedia.home.navigationscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.search.Search.Companion.SearchBar
import com.minhtu.firesocialmedia.home.search.SearchViewModel
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.UiUtils

class Friend {
    companion object{
        @Composable
        fun FriendScreen(modifier: Modifier,
                         paddingValues: PaddingValues,
                         searchViewModel: SearchViewModel = viewModel(),
                         homeViewModel: HomeViewModel,
                         onNavigateToUserInformation: (user : UserInstance) -> Unit,
                         onNavigateToShowImageScreen: (image : String) -> Unit){
            Column(verticalArrangement = Arrangement.Top,horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(paddingValues)) {
                val context = LocalContext.current
                SearchBar(query = searchViewModel.query,
                    onQueryChange = {
                        query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier.height(80.dp).padding(vertical = 10.dp)
                )
                Text(text = "Friends",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
                UiUtils.TabLayout(
                    listOf("Friends","Friend Requests"),
                    Friend.getScreenName(),
                    homeViewModel = homeViewModel,
                    searchViewModel = searchViewModel,
                    context = context,
                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                    onNavigateToUserInformation = onNavigateToUserInformation
                )
            }
        }

        fun getScreenName() : String {
            return "FriendScreen"
        }
    }
}