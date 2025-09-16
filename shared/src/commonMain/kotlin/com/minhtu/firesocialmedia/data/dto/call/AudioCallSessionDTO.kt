package com.minhtu.firesocialmedia.data.dto.call

import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import kotlinx.serialization.Serializable

@Serializable
data class AudioCallSessionDTO(
    var sessionId: String = "",
    var callerId: String = "",
    var calleeId: String = "",
    var offer: OfferAnswerDTO? = null,
    var answer: OfferAnswerDTO? = null,
    var callerCandidates: Map<String, IceCandidateDTO> = emptyMap(),
    var calleeCandidates: Map<String, IceCandidateDTO> = emptyMap(),
    var status: CallStatus = CallStatus.RINGING
)