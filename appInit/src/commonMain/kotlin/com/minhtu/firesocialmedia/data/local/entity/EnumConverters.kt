package com.minhtu.firesocialmedia.data.local.entity

import com.minhtu.firesocialmedia.home.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType

/**
 * Room [androidx.room.TypeConverters] for [AppDatabase][com.minhtu.firesocialmedia.data.local.room.AppDatabase].
 *
 * Lives in appInit (not feature/notification, where [NotificationEntity] now lives) because it
 * converts both [NotificationType] (notification-owned) and [DecentralizationType] (feature/home-owned,
 * used by feature/home's NewsEntity), so it spans two features. appInit is the only module that
 * legitimately depends on every feature, making it the natural home for this cross-feature
 * converter (Phase 3 decision, see instruction.md).
 */
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
