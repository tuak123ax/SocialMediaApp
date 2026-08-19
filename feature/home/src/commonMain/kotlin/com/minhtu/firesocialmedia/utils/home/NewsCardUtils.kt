package com.minhtu.firesocialmedia.utils.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.home.TestTag
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.storage.home.toStorageUrl
import com.minhtu.firesocialmedia.home.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.getUriStringFromLocalPath
import com.minhtu.firesocialmedia.home.utils.UiUtils.Companion.ExpandableText
import com.seiko.imageloader.ui.AutoSizeImage
import kotlin.math.roundToInt

class NewsCardUtils {
    companion object {
        @Composable
        fun SimpleNewsCard(
            news: NewsInstance,
            localImageLoaderValue: ProvidedValue<*>,
            modifier: Modifier = Modifier
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .testTag(TestTag.TAG_POST_IN_COLUMN)
                    .semantics { contentDescription = TestTag.TAG_POST_IN_COLUMN }
                    .then(modifier),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                            .fillMaxWidth()){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                news.avatar.toStorageUrl(),
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .testTag(TestTag.TAG_POSTER_AVATAR)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POSTER_AVATAR
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(news.timePosted),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                    ExpandableText(news.message)
                    if(news.image.isNotEmpty()){
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                news.image.toStorageUrl(),
                                contentDescription = "Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(5.dp)
                                    .testTag(TestTag.TAG_POST_IMAGE)
                                    .semantics{
                                        contentDescription = TestTag.TAG_POST_IMAGE
                                    }
                            )
                        }
                    } else {
                        if(news.video.isNotEmpty()) {
                            val videoUri: String = if(news.localPath.isNotEmpty()) {
                                //Load video from local storage
                                getUriStringFromLocalPath(news.localPath)
                            } else {
                                news.video.toStorageUrl()
                            }
                            if(videoUri.isNotEmpty()) {
                                VideoPlayer(
                                    videoUri,
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(5.dp)
                                        .testTag(TestTag.TAG_POST_VIDEO)
                                        .semantics{
                                            contentDescription = TestTag.TAG_POST_VIDEO
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun SimpleNewsCardSlideable(
            news: NewsInstance,
            localImageLoaderValue: ProvidedValue<*>,
            onSelected: (NewsInstance) -> Unit,
            onDelete: () -> Unit
        ) {
            val swipeDistancePx = with(LocalDensity.current) { 70.dp.toPx() }
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
            val swipeThreshold = -swipeDistancePx / 2

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .testTag(TestTag.TAG_DRAFT)
                    .semantics {
                        contentDescription = TestTag.TAG_DRAFT
                    }
            ) {
                //Row contains delete button
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .testTag(TestTag.TAG_BUTTON_DELETE)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_DELETE
                        },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Foreground content (slidable)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .pointerInput(news.id) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    val newOffset =
                                        (offsetX + dragAmount).coerceIn(-swipeDistancePx, 0f)
                                    offsetX = newOffset
                                },
                                onDragEnd = {
                                    offsetX = if (offsetX < swipeThreshold) -swipeDistancePx else 0f
                                }
                            )
                        }
                ) {
                    SimpleNewsCard(
                        news,
                        localImageLoaderValue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onSelected(news)
                            }
                    )
                }
            }
        }
    }
}
