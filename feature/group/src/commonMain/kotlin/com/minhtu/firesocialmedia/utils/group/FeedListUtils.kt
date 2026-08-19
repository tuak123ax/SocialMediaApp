package com.minhtu.firesocialmedia.utils.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.constants.group.DataConstant
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.storage.group.toStorageUrl
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.group.SessionViewModel
import com.minhtu.firesocialmedia.presentation.group.EngagementViewModel
import com.minhtu.firesocialmedia.presentation.groupdetails.PollViewModel
import com.minhtu.firesocialmedia.group.utils.UiUtils.Companion.NewsCard
import com.minhtu.firesocialmedia.group.utils.UiUtils.Companion.NewsCardPlaceholder
import com.minhtu.firesocialmedia.group.utils.UiUtils.Companion.NewsCardWithSharedContent
import com.minhtu.firesocialmedia.group.utils.UiUtils.Companion.ThreeDotsLoading
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FeedListUtils {
    companion object {
        /**
         * Renders a poll post card in the feed.
         * Shows the poster header, poll question, answer options with vote bars,
         * and a delete button for the post owner.
         *
         * Full poll detail (options, vote counts) is loaded separately from /polls/{pollId}
         * when needed. This card shows the question and a "View Poll" affordance.
         */
        @Composable
        fun PollCard(
            news: NewsInstance,
            user: UserInstance,
            localImageLoaderValue: ProvidedValue<*>,
            sessionViewModel: SessionViewModel,
            currentUserId: String = "",
            pollViewModel: PollViewModel? = null,
            onNavigateToUserInformation: (UserInstance?) -> Unit,
            onDelete: (NewsInstance) -> Unit
        ) {
            val currentUser = sessionViewModel.currentUser
            val vm = pollViewModel

            // ── Load full poll data lazily ──
            val pollId = news.pollId ?: ""
            LaunchedEffect(pollId) {
                if (pollId.isNotEmpty() && currentUserId.isNotEmpty()) {
                    vm?.loadPoll(pollId, currentUserId)
                }
            }

            val allPolls by (vm?.polls ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, com.minhtu.firesocialmedia.domain.entity.settings.group.PollObject>()) }).collectAsState()
            val allMyVotes by (vm?.myVotes ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, List<Int>>()) }).collectAsState()
            val allSubmitStates by (vm?.submitState ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, Boolean?>()) }).collectAsState()
            val allVotersMap by (vm?.allVoters ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, List<Pair<com.minhtu.firesocialmedia.group.entity.user.UserInstance?, List<Int>>>>()) }).collectAsState()

            val poll = allPolls[pollId]
            val myVotes = allMyVotes[pollId]          // null = not loaded yet; empty = loaded, no vote
            val isExpired = poll?.expiresAt?.let { it > 0 && getCurrentTime() > it } ?: false
            val isOwner = currentUser != null && currentUser.uid == news.posterId

            // Load voters when the owner has poll data
            LaunchedEffect(pollId, isOwner, poll) {
                if (isOwner && poll != null) {
                    vm?.loadAllVoters(pollId)
                }
            }

            val votersForPoll = allVotersMap[pollId]
            var showVoterDetails by remember(pollId) { mutableStateOf(false) }
            val hasVoted = myVotes != null && myVotes.isNotEmpty()
            // Show results if: user already voted, or poll is expired, or no options (edge case)
            val showResults = hasVoted || isExpired

            var pendingSelection by remember(pollId) { mutableStateOf<Set<Int>>(emptySet()) }
            val isSubmitting = false // submitState map uses null=idle, so we can't detect in-flight from it

            Card(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Header row ──
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable { onNavigateToUserInformation(user) }
                    ) {
                        CompositionLocalProvider(localImageLoaderValue) {
                            AutoSizeImage(
                                news.avatar.toStorageUrl(),
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        // Poll badge
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isExpired) "Closed" else "Poll",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        // Delete option — only for the post owner
                        if (isOwner) {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    CrossPlatformIcon(
                                        icon = "more_horiz",
                                        backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Delete Poll") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        },
                                        onClick = { showMenu = false; onDelete(news) }
                                    )
                                }
                            }
                        }
                    }

                    // ── Poll icon + question ──
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Poll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Poll", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = news.message.ifEmpty { poll?.question ?: "" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // ── Expiry info ──
                    val pollExpiresAt = poll?.expiresAt
                    if (pollExpiresAt != null && pollExpiresAt > 0) {
                        val remaining = pollExpiresAt - getCurrentTime()
                        val expiryText = when {
                            isExpired -> "Poll ended"
                            remaining < 60 * 60 * 1000L -> "Ends in <1 hour"
                            remaining < 24 * 60 * 60 * 1000L -> "Ends in ${remaining / (60 * 60 * 1000L)}h"
                            else -> "Ends in ${remaining / (24 * 60 * 60 * 1000L)}d"
                        }
                        Text(
                            text = expiryText,
                            fontSize = 11.sp,
                            color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Options (loading / voting / results) ──
                    if (poll == null) {
                        // Still loading
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading poll…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        val totalVotes = poll.votes?.values?.sum() ?: 0
                        poll.options.forEachIndexed { idx, option ->
                            val voteCount = poll.votes?.get(idx.toString()) ?: 0
                            val fraction = if (totalVotes > 0) voteCount.toFloat() / totalVotes else 0f
                            val isMyVote = myVotes?.contains(idx) == true
                            val isPending = pendingSelection.contains(idx)

                            if (showResults) {
                                // ── Results bar ──
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = option,
                                            fontSize = 14.sp,
                                            fontWeight = if (isMyVote) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${(fraction * 100).roundToInt()}%",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isMyVote) {
                                            Spacer(Modifier.width(4.dp))
                                            Text("✓", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                                .height(6.dp)
                                                .background(
                                                    if (isMyVote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                    RoundedCornerShape(3.dp)
                                                )
                                        )
                                    }
                                }
                            } else {
                                // ── Selectable option ──
                                val selected = isPending
                                OutlinedButton(
                                    onClick = {
                                        if (!isExpired && vm != null) {
                                            pendingSelection = if (poll.allowMultipleAnswers) {
                                                if (selected) pendingSelection - idx else pendingSelection + idx
                                            } else {
                                                setOf(idx)
                                            }
                                        }
                                    },
                                    enabled = !isExpired && vm != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    border = BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // ── Vote count + Submit button ──
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$totalVotes vote${if (totalVotes != 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!showResults && pendingSelection.isNotEmpty() && vm != null) {
                                Button(
                                    onClick = {
                                        vm.submitVote(pollId, currentUserId, pendingSelection.sorted())
                                        pendingSelection = emptySet()
                                    },
                                    enabled = !isSubmitting,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text("Vote", fontSize = 13.sp)
                                }
                            }
                            // Allow changing vote if already voted and poll still active
                            if (showResults && !isExpired && hasVoted && vm != null) {
                                TextButton(onClick = {
                                    // Clear local vote so the user sees selectable options again
                                    vm.clearMyVote(pollId)
                                    pendingSelection = emptySet()
                                }) {
                                    Text("Change vote", fontSize = 12.sp)
                                }
                            }
                        }

                        // ── Voter details button (poll owner only) ──
                        if (isOwner && poll != null) {
                            Spacer(Modifier.height(4.dp))
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            TextButton(
                                onClick = {
                                    // Compare cached voter count against poll vote totals.
                                    // votersForPoll.size = unique voters; totalVotes = sum of per-option counts.
                                    // For single-answer: they should be equal.
                                    // For multi-answer: totalVotes >= votersForPoll.size.
                                    // If the cached total (sum of indices across all voters) differs from
                                    // totalVotes, the cache is stale — refetch.
                                    val cachedVoteTotal = votersForPoll?.sumOf { (_, indices) -> indices.size } ?: -1
                                    if (votersForPoll == null || cachedVoteTotal < totalVotes) {
                                        vm?.refreshAllVoters(pollId)
                                    }
                                    showVoterDetails = true
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("See who voted", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // ── Voters BottomSheet ──
                            if (showVoterDetails) {
                                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                                ModalBottomSheet(
                                    onDismissRequest = { showVoterDetails = false },
                                    sheetState = sheetState,
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp)
                                    ) {
                                        // Sheet title
                                        Text(
                                            text = "Voters",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                                        )
                                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                        if (votersForPoll == null) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                            }
                                        } else if (votersForPoll.isEmpty()) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No votes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        } else {
                                            androidx.compose.foundation.lazy.LazyColumn(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                poll.options.forEachIndexed { idx, option ->
                                                    val votersForOption = votersForPoll.filter { (_, indices) -> indices.contains(idx) }
                                                    if (votersForOption.isNotEmpty()) {
                                                        item {
                                                            // Option header
                                                            Text(
                                                                text = option,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                                            )
                                                        }
                                                        items(votersForOption) { (user, _) ->
                                                            val displayName = user?.name?.ifEmpty { null } ?: "Unknown"
                                                            val avatarUrl = user?.image?.toStorageUrl() ?: ""
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                                            ) {
                                                                CompositionLocalProvider(localImageLoaderValue) {
                                                                    if (avatarUrl.isNotEmpty()) {
                                                                        AutoSizeImage(
                                                                            avatarUrl,
                                                                            contentDescription = "Avatar",
                                                                            contentScale = ContentScale.Crop,
                                                                            modifier = Modifier
                                                                                .size(40.dp)
                                                                                .clip(CircleShape)
                                                                        )
                                                                    } else {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(40.dp)
                                                                                .clip(CircleShape)
                                                                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Icon(
                                                                                Icons.Filled.Person,
                                                                                contentDescription = null,
                                                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                                modifier = Modifier.size(24.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                                Spacer(Modifier.width(12.dp))
                                                                Column {
                                                                    Text(
                                                                        text = displayName,
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    Text(
                                                                        text = "Voted for: $option",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            }
                                                            androidx.compose.material3.HorizontalDivider(
                                                                modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                    }
                }
            }
        }

        @Composable
        fun LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
            localImageLoaderValue: ProvidedValue<*>,
            listState: LazyListState,
            engagementViewModel: EngagementViewModel,
            sessionViewModel: SessionViewModel,
            list: List<NewsInstance>,
            onNavigateToUploadNews: (updateNew: NewsInstance?) -> Unit,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            showBottomSheet: (NewsInstance) -> Unit,
            groupId: String = "",
            pollViewModel: PollViewModel? = null,
            currentUserId: String = "",
            onDeletePoll: ((NewsInstance) -> Unit)? = null) {
            val coroutineScope = rememberCoroutineScope()
            val likeStatus by engagementViewModel.likedPosts.collectAsState()
            val likeCountList = engagementViewModel.likeCountList.collectAsState()
            val commentCountList = engagementViewModel.commentCountList.collectAsState()
            val loadedUsers by sessionViewModel.loadedUserState.collectAsState()
            // The root Scaffold only reserves the top safe-drawing inset (see SetUpNavigation in
            // Navigation.kt), so on screens without their own bottom bar - like this one - the
            // system navigation bar can overlap the last item's like/comment row. Pad the list's
            // content (not the list itself, which would just clip scrolling) by the nav bar's
            // height so the last post always comes to rest above it.
            val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag(TestTag.TAG_POSTS_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POSTS_COLUMN
                    },
                state = listState,
                contentPadding = PaddingValues(bottom = navigationBarPadding.calculateBottomPadding() + 16.dp)
            ) {
                items(
                    items = list,
                    key = { it.id }
                ) { news ->
                    var isVisible by remember(news.id) { mutableStateOf(true) }
                    val sharedNewMap by engagementViewModel.sharedNewsById.collectAsState()
                    LaunchedEffect(news.shareContentId) {
                        engagementViewModel.ensureSharedNew(news.shareContentId)
                    }
                    // news.posterId is only ever loaded into sessionViewModel's user cache via
                    // the Members tab or shared-post owners - never for a normal feed post.
                    // Without this, user is always null below, so every post falls through to
                    // NewsCardPlaceholder() forever instead of rendering.
                    LaunchedEffect(news.posterId) {
                        sessionViewModel.ensureUserLoaded(news.posterId)
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        exit = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 200)
                        )
                    ) {
                        val user = loadedUsers[news.posterId]
                        // Poll entries are invisible to old-app context (no pollViewModel) or home feed (no groupId)
                        val isPollWithoutSupport = news.type == DataConstant.POST_TYPE_POLL && (pollViewModel == null || groupId.isEmpty())
                        if(user != null && !isPollWithoutSupport) {
                            when {
                                news.type == DataConstant.POST_TYPE_POLL && groupId.isNotEmpty() -> {
                                    PollCard(
                                        news = news,
                                        user = user,
                                        localImageLoaderValue = localImageLoaderValue,
                                        sessionViewModel = sessionViewModel,
                                        currentUserId = currentUserId,
                                        pollViewModel = pollViewModel,
                                        onNavigateToUserInformation = onNavigateToUserInformation,
                                        onDelete = { deletedNews ->
                                            isVisible = false
                                            coroutineScope.launch {
                                                delay(250)
                                                if (onDeletePoll != null) {
                                                    onDeletePoll(deletedNews)
                                                } else {
                                                    val pollId = deletedNews.pollId
                                                    if (pollId != null) {
                                                        engagementViewModel.deletePoll(deletedNews.id, pollId, groupId)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                                news.shareContentId.isEmpty() -> {
                                NewsCard(
                                    news = news,
                                    user = user,
                                    isLiked = likeStatus.containsKey(news.id),
                                    likeCountList.value,
                                    commentCountList.value,
                                    localImageLoaderValue,
                                    likeCommentAndShareButtonEnable = true,
                                    hasDropdownMenu = true,
                                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                    onNavigateToUserInformation = onNavigateToUserInformation,
                                    engagementViewModel = engagementViewModel,
                                    sessionViewModel = sessionViewModel,
                                    listState = listState,
                                    onDelete = { action, deletedNews ->
                                        isVisible = false
                                        coroutineScope.launch {
                                            delay(250)
                                            if (action == "Delete") {
                                                engagementViewModel.deleteNews(deletedNews)
                                            }
                                        }
                                    },
                                    onNavigateToUploadNews,
                                    showBottomSheet
                                )
                                }
                                else -> {
                                val sharedNew = sharedNewMap[news.shareContentId]
                                if(sharedNew != null) {
                                    NewsCardWithSharedContent(
                                        news = news,
                                        sharedNew = sharedNew,
                                        user = user,
                                        isLiked = likeStatus.containsKey(news.id),
                                        likeCountList.value,
                                        commentCountList.value,
                                        localImageLoaderValue,
                                        onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                        onNavigateToUserInformation = onNavigateToUserInformation,
                                        engagementViewModel = engagementViewModel,
                                        sessionViewModel = sessionViewModel,
                                        listState = listState,
                                        onDelete = { action, deletedNews ->
                                            isVisible = false
                                            coroutineScope.launch {
                                                delay(250)
                                                if (action == "Delete") {
                                                    engagementViewModel.deleteNews(deletedNews)
                                                }
                                            }
                                        },
                                        onNavigateToUploadNews,
                                        showBottomSheet
                                    )
                                } else {
                                    NewsCardPlaceholder()
                                }
                                }
                            }
                        } else {
                            NewsCardPlaceholder()
                        }
                    }
                }

                // Loading row at the bottom
                if (engagementViewModel.isLoadingMore.value) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ThreeDotsLoading(
                                modifier = Modifier.padding(bottom = 10.dp),
                                dotSize = 10.dp,
                                spaceBetween = 5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
