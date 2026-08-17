package com.minhtu.firesocialmedia.data.local.mapper.comment

import com.minhtu.firesocialmedia.data.local.entity.HomeCommentEntity
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomRecord
import com.minhtu.firesocialmedia.home.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance

fun CommentInstance.toRoomRecord(selectedNewId: String): HomeCommentRoomRecord = HomeCommentRoomRecord(
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

fun HomeCommentRoomRecord.toRoomEntity(): HomeCommentEntity = HomeCommentEntity(
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

fun HomeCommentEntity.toRoomRecord(): HomeCommentRoomRecord = HomeCommentRoomRecord(
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

fun List<HomeCommentRoomRecord>.toDto() : List<CommentDTO> {
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
