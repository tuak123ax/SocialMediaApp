package com.minhtu.firesocialmedia.profile.data.remote.dto.news

import kotlinx.serialization.Serializable

/**
 * Result of a paginated posterId-filtered news fetch (see [com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService.getNewsByPoster]).
 * [lastTimePostedValue]/[lastKeyValue] are cursors into the *raw* feed (ordered by timePosted,
 * same as Home's pagination), not into the filtered [news] list — pass them back unchanged to
 * resume paging from where this page left off. Both are null once the raw feed is exhausted.
 */
@Serializable
data class ProfileLatestNewsDTO(
    val news: List<NewsDTO> = emptyList(),
    val lastTimePostedValue: Double? = null,
    val lastKeyValue: String? = null
)
