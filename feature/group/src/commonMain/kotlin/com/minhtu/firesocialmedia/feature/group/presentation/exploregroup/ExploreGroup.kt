package com.minhtu.firesocialmedia.feature.group.presentation.exploregroup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.core.storage.toStorageUrl
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.TitleAndSubTitleBelow
import com.minhtu.firesocialmedia.utils.Utils

import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.koin.compose.viewmodel.koinViewModel

class ExploreGroup {
    companion object {
        @Composable
        fun ExploreGroupScreen(
            currentUser : UserInstance,
            paddingValues: PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            exploreGroupViewModel: ExploreGroupViewModel = koinViewModel(),
            searchViewModel : SearchViewModel = koinViewModel(),
            onNavigateBack : () -> Unit,
            onNavigateToGroupDetails : (GroupInstance) -> Unit
        ) {
            CommonBackHandler {
                onNavigateBack()
            }
            val recommendGroups by exploreGroupViewModel.fetchRecommendGroups.collectAsState()
            val featureGroups by exploreGroupViewModel.fetchFeatureGroups.collectAsState()
            LaunchedEffect(searchViewModel.query) {
                exploreGroupViewModel.loadInitialRecommendGroups(currentUser, searchViewModel.query)
                exploreGroupViewModel.loadInitialFeatureGroups(currentUser, searchViewModel.query)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ){
                UiUtils.BackAndTitleAndMoreOptionsRow(
                    title = "Explore All Groups",
                    navigateBack = onNavigateBack
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
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                SectionHeaderAndClickableText(
                    header = "Featured Groups",
                    headerTextColor = MaterialTheme.colorScheme.onSurface,
                    subHeaderTextColor = MaterialTheme.colorScheme.primary,
                    onClickTrailingText = {

                    }
                )

                //Show row of featured groups
            val featuredState = rememberLazyListState()
            if(featureGroups.isNotEmpty()) {
                FeaturedGroupsRow(
                        listRecommendGroups = featureGroups,
                        localImageLoaderValue = localImageLoaderValue,
                    state = featuredState,
                        onSelectGroup = { group ->
                            onNavigateToGroupDetails(group)
                        }
                    )
                }

                SectionHeaderAndClickableText(
                    header = "Recommended for you",
                    headerTextColor = MaterialTheme.colorScheme.onSurface
                )
                //Show list of recommended groups
            val recommendedState = rememberLazyListState()
            if(recommendGroups.isNotEmpty()) {
                RecommendedGroupColumn(
                        listRecommendGroups = recommendGroups,
                        localImageLoaderValue = localImageLoaderValue,
                    state = recommendedState,
                        onClickJoinButton = { group ->
                            onNavigateToGroupDetails(group)
                        }
                    )
                }
            // Load more when featured row scrolled to end
            LaunchedEffect(featuredState, searchViewModel.query) {
                snapshotFlow {
                    val layoutInfo = featuredState.layoutInfo
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    Pair(lastVisible, layoutInfo.totalItemsCount)
                }
                    .map { (lastVisible, total) -> total > 0 && lastVisible >= total - 3 }
                    .distinctUntilChanged()
                    .filter { it }
                    .collect {
                        exploreGroupViewModel.loadMoreFeatureGroups(currentUser, searchViewModel.query)
                    }
            }
            // Load more when recommended column scrolled to end
            LaunchedEffect(recommendedState, searchViewModel.query) {
                snapshotFlow {
                    val layoutInfo = recommendedState.layoutInfo
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    Pair(lastVisible, layoutInfo.totalItemsCount)
                }
                    .map { (lastVisible, total) -> total > 0 && lastVisible >= total - 3 }
                    .distinctUntilChanged()
                    .filter { it }
                    .collect {
                        exploreGroupViewModel.loadMoreRecommendGroups(currentUser, searchViewModel.query)
                    }
            }
            }
        }
        fun getScreenName() : String {
            return "ExploreGroupScreen"
        }

        @Composable
        fun SectionHeaderAndClickableText(header : String,
                                          subHeader: String = "",
                                          headerTextColor : Color? = null,
                                          subHeaderTextColor : Color? = null,
                                          onClickTrailingText : () -> Unit = {}) {
            val resolvedHeader = headerTextColor ?: MaterialTheme.colorScheme.onSurface
            val resolvedSubHeader = subHeaderTextColor ?: MaterialTheme.colorScheme.primary
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = header,
                    color = resolvedHeader,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                if(subHeader.isNotEmpty()) {
                    Text(
                        text = subHeader,
                        color = resolvedSubHeader,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clickable{
                                onClickTrailingText()
                            }
                    )
                }
            }
        }

        @Composable
        fun FeaturedGroupsRow(
            listRecommendGroups : List<GroupInstance>,
            localImageLoaderValue : ProvidedValue<*>,
            state: LazyListState,
            onSelectGroup : (GroupInstance) -> Unit
        ) {
            LazyRow(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)
                    .padding(vertical = 10.dp)
            ) {
                items(listRecommendGroups) { group ->
                    FeatureGroupItem(
                        group,
                        localImageLoaderValue,
                        onSelectGroup = {
                            onSelectGroup(group)
                        }
                    )
                }
            }
        }

        @Composable
        fun FeatureGroupItem(
            group : GroupInstance,
            localImageLoaderValue : ProvidedValue<*>,
            onSelectGroup : () -> Unit
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clickable {
                        onSelectGroup()
                    }
            ) {
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        group.avatar.toStorageUrl(),
                        contentDescription = "group avatar",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(150.dp)
                            .width(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    TitleAndSubTitleBelow(
                        group.name,
                        Utils.convertToNumberString(group.members.size) + if(group.members.size > 1) " members" else " member",
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        @Composable
        fun RecommendedGroupColumn(
            listRecommendGroups : List<GroupInstance>,
            localImageLoaderValue : ProvidedValue<*>,
            state: LazyListState,
            onClickJoinButton : (GroupInstance) -> Unit
        ) {
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                items(listRecommendGroups) { group ->
                    RecommendedGroupCard(
                        group,
                        localImageLoaderValue,
                        onClickJoinButton = {
                            onClickJoinButton(group)
                        }
                    )
                }
            }
        }

        @Composable
        fun RecommendedGroupCard(
            group : GroupInstance,
            localImageLoaderValue : ProvidedValue<*>,
            onClickJoinButton : () -> Unit
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .testTag(TestTag.TAG_RECOMMEND_GROUP)
                    .semantics {
                        contentDescription = TestTag.TAG_RECOMMEND_GROUP
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    CompositionLocalProvider(localImageLoaderValue) {
                        AutoSizeImage(
                            group.avatar.toStorageUrl(),
                            contentDescription = "Group Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        TitleAndSubTitleBelow(
                            group.name,
                            Utils.convertToNumberString(group.members.size) +
                                    if (group.members.size > 1) " members" else " member",
                            textAlign = TextAlign.Start
                        )
                    }

                    OutlinedButton(
                        onClick = onClickJoinButton,
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Join",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

            }
        }
    }
}