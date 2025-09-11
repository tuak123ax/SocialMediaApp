package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.utils.Utils

interface CallRepository {
    suspend fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String
    ) : Boolean?

    suspend fun startCallService(
        sessionId : String,
        caller : UserInstance,
        callee : UserInstance
    )

    suspend fun startVideoCallService(
        sessionId : String,
        caller : UserInstance,
        callee : UserInstance,
        currentUserId : String?,
        remoteVideoOffer : OfferAnswerDTO?
    )
}