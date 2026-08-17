package com.minhtu.firesocialmedia.domain.usecases.news.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance

class GetNewByIdUseCase(
    private val groupNewsRepository: GroupNewsRepository
) {
    suspend operator fun invoke(newId: String): NewsInstance? {
        return groupNewsRepository.getNew(newId)
    }
}
