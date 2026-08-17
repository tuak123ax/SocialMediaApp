package com.minhtu.firesocialmedia.entity.bridge

import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO as HomeUserDTO
import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO as AuthUserDTO
import com.minhtu.firesocialmedia.friend.data.remote.dto.user.UserDTO as FriendUserDTO
import com.minhtu.firesocialmedia.comment.data.remote.dto.user.UserDTO as CommentUserDTO
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO as NotificationUserDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO as ProfileUserDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO as GroupUserDTO
import com.minhtu.firesocialmedia.calling.data.remote.dto.user.UserDTO as CallingUserDTO
import com.minhtu.firesocialmedia.security.data.remote.dto.user.UserDTO as SecurityUserDTO

/**
 * Bridges between each feature's own local UserDTO fork and appInit's own local [UserDTO].
 * appInit is the composition root that already bridges feature-local types for navigation,
 * and its own `search/entity/user/UserMapper.kt` (`toSearchUser()`/`toDto()`) uses appInit's
 * local UserDTO as the hub type, so this is the right place for cross-feature conversions.
 */
fun HomeUserDTO.toAppInitUserDto(): UserDTO = UserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun AuthUserDTO.toAppInitUserDto(): UserDTO = UserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun FriendUserDTO.toAppInitUserDto(): UserDTO = UserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun CommentUserDTO.toAppInitUserDto(): UserDTO = UserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun UserDTO.toCommentUserDto(): CommentUserDTO = CommentUserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun NotificationUserDTO.toAppInitUserDto(): UserDTO = UserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun ProfileUserDTO.toAppInitUserDto(): UserDTO = UserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun UserDTO.toProfileUserDto(): ProfileUserDTO = ProfileUserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun GroupUserDTO.toAppInitUserDto(): UserDTO = UserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun UserDTO.toSecurityUserDto(): SecurityUserDTO = SecurityUserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun UserDTO.toGroupUserDto(): GroupUserDTO = GroupUserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)

fun UserDTO.toCallingUserDto(): CallingUserDTO = CallingUserDTO(
    email = email, image = image, name = name, status = status, phone = phone, token = token, uid = uid,
    background = background, likedPosts = likedPosts, friendRequests = friendRequests,
    friends = friends, likedComments = likedComments, groups = groups,
    lastTimeChangePassword = lastTimeChangePassword, twoFAEnabled = twoFAEnabled,
    lastTimeReadPrivacy = lastTimeReadPrivacy, lastTimeAcknowledgedLoginHistory = lastTimeAcknowledgedLoginHistory
)
