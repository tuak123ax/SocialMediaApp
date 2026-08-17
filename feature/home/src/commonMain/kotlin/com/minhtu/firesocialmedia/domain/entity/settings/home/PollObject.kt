package com.minhtu.firesocialmedia.domain.entity.settings.home
data class PollObject(
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
    // Computed on creation from duration; null = legacy / no expiry
    val expiresAt: Long? = null,
    // votes: optionIndex (String) -> count; kept as domain type
    val votes: Map<String, Int>? = null,
)
