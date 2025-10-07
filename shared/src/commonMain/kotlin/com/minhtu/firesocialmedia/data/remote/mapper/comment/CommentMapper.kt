package com.minhtu.firesocialmedia.data.remote.mapper.comment

import com.minhtu.firesocialmedia.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance

// Use idKey (the map key) as a fallback if dto.id is blank.
fun CommentDTO.toDomain(idKey: String? = null): CommentInstance {
    val realId = if (id.isNotBlank()) id else idKey.orEmpty()
    return CommentInstance(
        id = realId,
        posterId = posterId,
        posterName = posterName,
        avatar = avatar,
        message = message,
        video = video,
        image = image,
        listReplies = listReplies.toDomainMap(), // recursive
        likeCount = likeCount,
        commentCount = commentCount,
        timePosted = timePosted
    )
}

fun Map<String, CommentDTO>?.toDomainMap(): HashMap<String, CommentInstance> {
    if (this == null) return hashMapOf()
    val out = HashMap<String, CommentInstance>(this.size)
    for ((key, dto) in this) {
        out[key] = dto.toDomain(key) // pass key so id fallback works
    }
    return out
}