package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class CopyLinkUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(copyData : String) {
        groupRepository.copyLink(copyData)
    }
}
