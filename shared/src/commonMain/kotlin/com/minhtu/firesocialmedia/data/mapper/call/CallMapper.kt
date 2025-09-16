package com.minhtu.firesocialmedia.data.mapper.call

import com.minhtu.firesocialmedia.data.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer

fun AudioCallSessionDTO.toDomain() : AudioCallSession {
    return AudioCallSession(
        sessionId,
        callerId,
        calleeId,
        offer?.toDomain(),
        answer?.toDomain(),
        callerCandidates.toDomainCandidates(),
        calleeCandidates.toDomainCandidates(),
        status
    )
}

fun AudioCallSession.toDto() : AudioCallSessionDTO {
    return AudioCallSessionDTO(
        sessionId,
        callerId,
        calleeId,
        offer?.toDto(),
        answer?.toDto(),
        callerCandidates.toDtoCandidates(),
        calleeCandidates.toDtoCandidates(),
        status
    )
}

fun IceCandidateDTO.toDomain() : IceCandidateData {
    return IceCandidateData(
        candidate,
        sdpMid,
        sdpMLineIndex
    )
}

fun IceCandidateData.toDto() : IceCandidateDTO {
    return IceCandidateDTO(
        candidate,
        sdpMid,
        sdpMLineIndex
    )
}

fun Map<String, IceCandidateDTO>?.toDomainCandidates(): Map<String, IceCandidateData> =
    this?.mapValues { (_, v) -> v.toDomain() } ?: emptyMap()

fun Map<String, IceCandidateData>?.toDtoCandidates(): HashMap<String, IceCandidateDTO> =
    if (this == null) hashMapOf()
    else entries.associateTo(HashMap()) { (k, v) -> k to v.toDto() }

fun OfferAnswerDTO.toDomain() : OfferAnswer {
    return OfferAnswer(
        sdp,
        type,
        initiator
    )
}

fun OfferAnswer.toDto() : OfferAnswerDTO {
    return OfferAnswerDTO(
        sdp,
        type,
        initiator
    )
}