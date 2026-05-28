package com.minhtu.firesocialmedia.core.domain.usecases.group

import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository

class CopyLinkUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(copyData : String) {
        groupRepository.copyLink(copyData)
    }
}