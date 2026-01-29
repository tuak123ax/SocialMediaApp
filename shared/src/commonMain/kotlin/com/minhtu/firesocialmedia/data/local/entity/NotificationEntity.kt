package com.minhtu.firesocialmedia.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.core.DecentralizationType

@Entity(
    tableName = "Notifications",
    indices = [Index(value = ["sender", "timeSend"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val content: String = "",
    val avatar: String = "",
    val sender: String = "",
    val timeSend: Long = 0L,
    val type: NotificationType = NotificationType.NONE,
    val relatedInfo: String = "",
    var beRead : Boolean = false
)

class EnumConverters {
    @androidx.room.TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.name

    @androidx.room.TypeConverter
    fun toNotificationType(value: String): NotificationType =
        runCatching { NotificationType.valueOf(value) }.getOrElse { NotificationType.NONE }

    @androidx.room.TypeConverter
    fun fromDecentralizationType(type: DecentralizationType?): String? = when (type) {
        is DecentralizationType.Public -> "PUBLIC"
        is DecentralizationType.Private -> "PRIVATE"
        is DecentralizationType.OnlyFriends -> "ONLY_FRIENDS"
        null -> null
    }

    @androidx.room.TypeConverter
    fun toDecentralizationType(value: String?): DecentralizationType? = when (value) {
        "PUBLIC" -> DecentralizationType.Public
        "PRIVATE" -> DecentralizationType.Private
        "ONLY_FRIENDS" -> DecentralizationType.OnlyFriends
        else -> null
    }
}