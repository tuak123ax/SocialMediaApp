package com.minhtu.firesocialmedia.data.remote.dto.settings

import kotlinx.serialization.Serializable

@Serializable
data class PollDTO(
    val id : String = "",
    val posterId : String = "",
    val posterName : String = "",
    val posterAvatar : String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val allowMultipleAnswers: Boolean = false,
    val duration: String = "",
    var groupId : String = "",
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var timePosted: Long = 0,
    // Backward-compatible: null → no expiry stored yet (treat as non-expiring)
    val expiresAt: Long? = null,
    // votes: optionIndex (as String) → voteCount; null = no votes yet
    val votes: Map<String, Int>? = null,
)