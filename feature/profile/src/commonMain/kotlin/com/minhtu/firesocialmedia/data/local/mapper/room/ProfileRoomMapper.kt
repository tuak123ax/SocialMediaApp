package com.minhtu.firesocialmedia.data.local.mapper.room

import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO

fun UserDTO.toRoomEntity() : UserEntity {
    return UserEntity(
        email, image, name, status, phone, token, uid, background
    )
}

fun UserEntity.toDomain() : UserDTO {
    return UserDTO(
        email = email,
        image = image,
        name = name,
        status = status,
        phone = phone,
        token = token,
        uid = uid,
        background = background
    )
}
