package com.minhtu.firesocialmedia.domain.entity.call

sealed class SpeakerType {
    object Speaker : SpeakerType()
    object Audio : SpeakerType()
}
