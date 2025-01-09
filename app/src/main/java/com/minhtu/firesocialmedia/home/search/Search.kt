package com.minhtu.firesocialmedia.home.search

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.UserInstance

class Search {
    companion object{
        @Composable
        fun SearchScreen(modifier: Modifier,
                         searchViewModel: SearchViewModel,
                         homeViewModel: HomeViewModel,
                         onNavigateToUserInformation: (user : UserInstance) -> Unit){
            Column(verticalArrangement = Arrangement.Top, modifier = modifier) {
                val context = LocalContext.current
                SearchBar(query = searchViewModel.query, onQueryChange = {
                    query -> searchViewModel.updateQuery(query)
                })

                // Filtered List
                LazyColumn {
                   val filterList = homeViewModel.listUsers?.filter { user ->
                       user.name.contains(searchViewModel.query, ignoreCase = true)
                   }?: emptyList()
                    items(filterList){user ->
                        UserRow(user, context, onNavigateToUserInformation)
                    }
                }
            }
        }

        @Composable
        private fun UserRow(user : UserInstance, context : Context, onNavigateToUserInfomation: (user: UserInstance) -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(5.dp)
                    .fillMaxWidth()
                    .border(1.dp, Color.Black, RectangleShape)
                    .clickable {
                        onNavigateToUserInfomation(user)
                    }){
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(user.image))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(5.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(1.dp))
                Text(
                    text = user.name,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 5.dp) // Adds padding around text
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
            placeholder: String = "Search..."
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = {Text(text = placeholder)},
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}