package com.minhtu.firesocialmedia.calling.data.remote.dto.group

import kotlinx.serialization.Serializable

@Serializable
data class GroupSummaryDTO(
    val id: String = "",
    val name: String = "",
    val avatar: String = "",
    var notificationOn : Boolean = false
)
