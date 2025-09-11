package com.minhtu.firesocialmedia.data.mapper.user

import com.minhtu.firesocialmedia.data.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.data.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.mapper.notification.toDomain
import com.minhtu.firesocialmedia.data.mapper.notification.toDto
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

fun UserDTO.toDomain() : UserInstance {
    return UserInstance(
        email,
        image,
        name,
        status,
        token,
        uid,
        likedPosts,
        friendRequests,
        notifications.toDomainNotifications(),
        friends,
        likedComments
    )
}

fun UserInstance.toDto() : UserDTO {
    return UserDTO(
        email,
        image,
        name,
        status,
        token,
        uid,
        likedPosts,
        friendRequests,
        notifications.toDTONotifications(),
        friends,
        likedComments
    )
}

fun ArrayList<NotificationDTO>.toDomainNotifications() : ArrayList<NotificationInstance> =
    ArrayList(this.map { item -> item.toDomain() })

fun ArrayList<NotificationInstance>.toDTONotifications() : ArrayList<NotificationDTO> =
    ArrayList(this.map { item -> item.toDto() })