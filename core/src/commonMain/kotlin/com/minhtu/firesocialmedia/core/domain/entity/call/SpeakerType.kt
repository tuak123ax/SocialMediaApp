package com.minhtu.firesocialmedia.core.domain.entity.call

sealed class SpeakerType {
    object Speaker : SpeakerType()
    object Audio : SpeakerType()
}
