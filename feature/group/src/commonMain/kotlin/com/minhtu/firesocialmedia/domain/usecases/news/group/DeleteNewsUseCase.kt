package com.minhtu.firesocialmedia.domain.usecases.news.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance

class DeleteNewsUseCase(
    private val groupNewsRepository: GroupNewsRepository
) {
    suspend operator fun invoke(new: NewsInstance): Boolean {
        return groupNewsRepository.deleteNewsFromDatabase(new)
    }
}
