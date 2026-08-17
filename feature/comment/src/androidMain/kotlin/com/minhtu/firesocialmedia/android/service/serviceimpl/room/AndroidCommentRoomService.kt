package com.minhtu.firesocialmedia.android.service.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.dao.CommentDao
import com.minhtu.firesocialmedia.data.local.mapper.comment.toRoomEntity
import com.minhtu.firesocialmedia.data.local.mapper.comment.toRoomRecord
import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomRecord
import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomService

class AndroidCommentRoomService(
    private val commentDao: CommentDao
) : CommentRoomService {
    override suspend fun saveComment(comment: CommentRoomRecord) {
        commentDao.saveComment(comment.toRoomEntity())
    }

    override suspend fun getAllComments(): List<CommentRoomRecord> {
        return commentDao.getAllComments().map { it.toRoomRecord() }
    }

    override suspend fun clearComments() {
        commentDao.clear()
    }

    override suspend fun hasComment(): Boolean {
        return commentDao.hasAnyComments()
    }
}
