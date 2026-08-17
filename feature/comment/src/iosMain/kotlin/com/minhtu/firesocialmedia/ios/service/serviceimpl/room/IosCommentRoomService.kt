package com.minhtu.firesocialmedia.ios.service.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomRecord
import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomService

/**
 * No-op stub, matching the original (pre-Phase-3) `IosRoomService` convention: iOS Room access is
 * not wired up to a real database (see instruction.md).
 */
class IosCommentRoomService : CommentRoomService {
    override suspend fun saveComment(comment: CommentRoomRecord) {
        // no-op
    }

    override suspend fun getAllComments(): List<CommentRoomRecord> {
        return emptyList()
    }

    override suspend fun clearComments() {
        // no-op
    }

    override suspend fun hasComment(): Boolean {
        return false
    }
}
