package com.minhtu.firesocialmedia.domain.entity.group

import kotlinx.serialization.Serializable

@Serializable
data class GroupConfigs(
    val id: String = "",
    val name: String = "",
    val avatar: String = "",
    var notificationOn : Boolean = false
)
