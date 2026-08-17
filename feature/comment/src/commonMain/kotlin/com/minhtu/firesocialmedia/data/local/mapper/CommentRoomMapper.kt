package com.minhtu.firesocialmedia.data.local.mapper.comment

import com.minhtu.firesocialmedia.data.local.entity.CommentEntity
import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomRecord
import com.minhtu.firesocialmedia.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance

fun CommentInstance.toRoomRecord(selectedNewId: String): CommentRoomRecord = CommentRoomRecord(
    id,
    posterId,
    posterName,
    avatar,
    message,
    video,
    image,
    likeCount,
    commentCount,
    timePosted,
    selectedNewId
)

fun CommentRoomRecord.toRoomEntity(): CommentEntity = CommentEntity(
    id,
    posterId,
    posterName,
    avatar,
    message,
    video,
    image,
    likeCount,
    commentCount,
    timePosted,
    selectedNewId
)

fun CommentEntity.toRoomRecord(): CommentRoomRecord = CommentRoomRecord(
    id,
    posterId,
    posterName,
    avatar,
    message,
    video,
    image,
    likeCount,
    commentCount,
    timePosted,
    selectedNewId
)

fun List<CommentRoomRecord>.toDto() : List<CommentDTO> {
    return this.map { it -> CommentDTO(
        it.id,
        it.posterId,
        it.posterName,
        it.avatar,
        it.message,
        it.video,
        it.image,
        likeCount = it.likeCount,
        commentCount = it.commentCount,
        timePosted = it.timePosted,
        selectedNewId = it.selectedNewId
    ) }
}
