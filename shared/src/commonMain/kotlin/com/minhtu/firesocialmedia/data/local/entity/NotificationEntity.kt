package com.minhtu.firesocialmedia.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType

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
    val relatedInfo: String = ""
)

class EnumConverters {
    @androidx.room.TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.name

    @androidx.room.TypeConverter
    fun toNotificationType(value: String): NotificationType =
        runCatching { NotificationType.valueOf(value) }.getOrElse { NotificationType.NONE }
}