package com.minhtu.firesocialmedia.security.entity.user

import com.minhtu.firesocialmedia.security.data.remote.dto.user.UserDTO

fun UserDTO.toSecurityUser(): UserInstance = UserInstance(
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

fun UserInstance.toDto(): UserDTO = UserDTO(
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
