package com.minhtu.firesocialmedia.data.remote.mapper.group

import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDto
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance

fun GroupInstance.toDto() : GroupDTO {
    return GroupDTO(
        id,
        name,
        avatar,
        password,
        description,
        createdDate,
        members,
        HashMap(posts.mapValues { (_,v) -> v.toDto() })
    )
}

fun GroupDTO.toDomain() : GroupInstance {
    return GroupInstance(
        id,
        name,
        avatar,
        password,
        description,
        createdDate,
        members,
        HashMap(posts.mapValues { (_,v) -> v.toDomain() })
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