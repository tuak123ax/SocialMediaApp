package com.minhtu.firesocialmedia.data.repository.local

import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomService

class ClearCommentLocalDataUseCaseImpl(
    private val commentRoomService: CommentRoomService
) {
    suspend operator fun invoke() {
        commentRoomService.clearComments()
    }
}
