package com.minhtu.firesocialmedia.data.remote.dto.settings.home

import kotlinx.serialization.Serializable

/**
 * Feature-home-owned copy of the Poll DTO, mirroring feature/group's and feature/profile's own
 * copies. Kept identical in shape to the others so mapping stays trivial, but each feature owns
 * its copy independently so core's `DatabaseService` no longer needs to know about polls.
 */
@Serializable
data class PollDTO(
    val id: String = "",
    val posterId: String = "",
    val posterName: String = "",
    val posterAvatar: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val allowMultipleAnswers: Boolean = false,
    val duration: String = "",
    val groupId: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val timePosted: Long = 0,
    // Backward-compatible: null → no expiry stored yet (treat as non-expiring)
    val expiresAt: Long? = null,
    // votes: optionIndex (as String) → voteCount; null = no votes yet
    val votes: Map<String, Int>? = null,
)
