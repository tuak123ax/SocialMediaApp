package com.minhtu.firesocialmedia.data.remote.mapper.group

import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance

fun GroupInstance.toDto() : GroupDTO {
    return GroupDTO(
        id,
        name,
        avatar,
        password,
        createdDate,
        members,
        posts
    )
}

fun GroupDTO.toDomain() : GroupInstance {
    return GroupInstance(
        id,
        name,
        avatar,
        password,
        createdDate,
        members,
        posts
    )
}
fun GroupSummaryDTO.toGroupDTO() : GroupDTO {
    return GroupDTO(
        id,
        name,
        avatar
    )
}

fun GroupDTO.GroupSummaryDTO() : GroupSummaryDTO {
    return GroupSummaryDTO(
        id,
        name,
        avatar
    )
}