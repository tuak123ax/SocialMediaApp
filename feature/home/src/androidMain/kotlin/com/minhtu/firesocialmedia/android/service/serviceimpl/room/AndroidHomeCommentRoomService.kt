package com.minhtu.firesocialmedia.android.service.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.dao.HomeCommentDao
import com.minhtu.firesocialmedia.data.local.mapper.comment.toRoomEntity
import com.minhtu.firesocialmedia.data.local.mapper.comment.toRoomRecord
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomRecord
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomService

class AndroidHomeCommentRoomService(
    private val commentDao: HomeCommentDao
) : HomeCommentRoomService {
    override suspend fun saveComment(comment: HomeCommentRoomRecord) {
        commentDao.saveComment(comment.toRoomEntity())
    }

    override suspend fun getAllComments(): List<HomeCommentRoomRecord> {
        return commentDao.getAllComments().map { it.toRoomRecord() }
    }

    override suspend fun clearComments() {
        commentDao.clear()
    }

    override suspend fun hasComment(): Boolean {
        return commentDao.hasAnyComments()
    }
}
