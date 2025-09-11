package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.mapper.user.toDto
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.domain.service.call.AudioCallService
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService

class CallRepositoryImpl(
    private val databaseService: DatabaseService,
    private val callService: AudioCallService
) : CallRepository {
    override suspend fun isCalleeInActiveCall(calleeId: String, callPath: String) : Boolean? {
        return databaseService.isCalleeInActiveCall(calleeId, callPath)
    }

    override suspend fun startCallService(
        sessionId: String,
        caller: UserInstance,
        callee: UserInstance
    ) {
        callService.startCallService(sessionId, caller.toDto(), callee.toDto())
    }

    override suspend fun startVideoCallService(
        sessionId: String,
        caller: UserInstance,
        callee: UserInstance,
        currentUserId: String?,
        remoteVideoOffer: OfferAnswerDTO?
    ) {
        callService.startVideoCallService(
            sessionId,
            caller.toDto(),
            callee.toDto(),
            currentUserId,
            remoteVideoOffer
        )
    }
}