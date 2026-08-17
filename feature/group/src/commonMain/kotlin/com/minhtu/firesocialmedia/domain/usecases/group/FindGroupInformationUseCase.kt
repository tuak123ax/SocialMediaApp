package com.minhtu.firesocialmedia.domain.usecases.group

import com.minhtu.firesocialmedia.domain.repository.GroupRepository

class FindGroupInformationUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke() {

    }
}
