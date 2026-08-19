package com.minhtu.firesocialmedia.profile.entity.news

/**
 * One page of a specific user's posts, plus cursors to resume fetching the next page.
 * [lastTimePosted]/[lastKey] are null once there are no more posts left to page through
 * (regardless of whether [news] on this page happened to be empty).
 */
data class ProfileNewsPage(
    val news: List<NewsInstance>,
    val lastTimePosted: Double?,
    val lastKey: String?
)
