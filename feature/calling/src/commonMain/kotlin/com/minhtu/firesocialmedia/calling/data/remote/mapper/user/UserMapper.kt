package com.minhtu.firesocialmedia.calling.data.remote.mapper.user

import com.minhtu.firesocialmedia.calling.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.calling.entity.user.UserInstance

fun UserDTO.toDomain() : UserInstance {
    return UserInstance(
        email = email,
        image = image,
        name = name,
        status = status,
        phone = phone,
        token = token,
        uid = uid,
        background = background,
        likedPosts = likedPosts,
        friendRequests = friendRequests,
        friends = friends,
        likedComments = likedComments,
        groups = HashSet(groups.keys),
        lastTimeChangePassword = lastTimeChangePassword,
        twoFAEnabled = twoFAEnabled,
        lastTimeReadPrivacy = lastTimeReadPrivacy,
        lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
    )
}

fun UserInstance.toDto(): UserDTO {
    return UserDTO(
        email = email,
        image = image,
        name = name,
        status = status,
        phone = phone,
        token = token,
        uid = uid,
        background = background,
        likedPosts = likedPosts,
        friendRequests = friendRequests,
        friends = friends,
        likedComments = likedComments,
        groups = HashMap(groups.associateWith { true }),
        lastTimeChangePassword = lastTimeChangePassword,
        twoFAEnabled = twoFAEnabled,
        lastTimeReadPrivacy = lastTimeReadPrivacy,
        lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
    )
}
