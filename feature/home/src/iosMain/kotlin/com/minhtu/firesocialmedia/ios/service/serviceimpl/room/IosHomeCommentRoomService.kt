package com.minhtu.firesocialmedia.ios.service.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomRecord
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomService

/**
 * No-op stub, matching the original (pre-Phase-3) `IosRoomService` convention: iOS Room access is
 * not wired up to a real database (see instruction.md).
 */
class IosHomeCommentRoomService : HomeCommentRoomService {
    override suspend fun saveComment(comment: HomeCommentRoomRecord) {
        // no-op
    }

    override suspend fun getAllComments(): List<HomeCommentRoomRecord> {
        return emptyList()
    }

    override suspend fun clearComments() {
        // no-op
    }

    override suspend fun hasComment(): Boolean {
        return false
    }
}
