package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.seiko.imageloader.ui.AutoSizeImage

class SelectGroup {
    companion object {
        @Composable
        fun SelectGroupScreen(
            currentUser : UserInstance,
            selectGroupViewModel: SelectGroupViewModel,
            searchViewModel : SearchViewModel,
            paddingValues : PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            onNavigateBack : () -> Unit,
            onNavigateToCreateGroup : () -> Unit,
            onNavigateToSelectedGroup : (GroupInstance) -> Unit
        ) {
            CommonBackHandler {
                onNavigateBack()
            }
            val groupList = currentUser.groups.values
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = "My Groups",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                    FloatingActionButton(
                        onClick = {
                            if(groupList.size < 50) {
                                onNavigateToCreateGroup()
                            } else {
                                showToast("You only can join 50 groups at the same time!")
                            }
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp),
                        modifier = Modifier
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Search.SearchBar(
                    query = searchViewModel.query,
                    onQueryChange = { query -> searchViewModel.updateQuery(query) },
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .testTag(TestTag.TAG_SEARCH_BAR)
                        .semantics {
                            contentDescription = TestTag.TAG_SEARCH_BAR
                        }
                )

//                Row(
//                    modifier = Modifier.fillMaxWidth().padding(20.dp)
//                ) {
//                    Icon(Icons.Default.PushPin, contentDescription = "Pin", tint = Color.Red)
//                    Spacer(Modifier.width(10.dp))
//                    Text(
//                        text = "PINNED",
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Gray
//                    )
//                }
                //Pinned groups
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(20.dp)
//                ) {
//                    items(
//                        items = groupList.toList(),
//                        key = {it}
//                    ) {
//
//                    }
//                }

                Text(
                    text = "ALL GROUPS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, start = 20.dp)
                )
                //All groups
                var filterList by remember { mutableStateOf<List<GroupInstance>>(emptyList()) }
                // Run filtering when friend list or search query changes
                LaunchedEffect( searchViewModel.query) {
                    filterList = groupList.filter { it.name.contains(searchViewModel.query) }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(
                        items = filterList,
                        key = {it.id}
                    ) {
                        GroupCard(it,
                            localImageLoaderValue,
                            onNavigateToSelectedGroup)
                    }
                }
            }
        }
        fun getScreenName() : String {
            return "SelectGroupScreen"
        }

        @Composable
        fun GroupCard(
            group : GroupInstance,
            localImageLoaderValue : ProvidedValue<*>,
            onNavigateToSelectedGroup : (GroupInstance) -> Unit
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable {
                        onNavigateToSelectedGroup(group)
                    }
            ) {
                Spacer(Modifier.width(20.dp))
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        group.avatar.toStorageUrl(),
                        contentDescription = "Group Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                }
                Text(
                    text = group.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp))
                Spacer(Modifier.weight(1f))
                CrossPlatformIcon(
                    icon = "right_arrow",
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    contentDescription = "right_arrow",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(20.dp)
                )
                Spacer(Modifier.width(20.dp))
            }
        }
    }
}