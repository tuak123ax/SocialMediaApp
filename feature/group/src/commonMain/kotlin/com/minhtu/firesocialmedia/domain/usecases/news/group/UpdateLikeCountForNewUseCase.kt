package com.minhtu.firesocialmedia.domain.usecases.news.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository

class UpdateLikeCountForNewUseCase(
    private val groupNewsRepository: GroupNewsRepository
) {
    suspend operator fun invoke(newsId: String, value: Int) {
        groupNewsRepository.updateLikeCountForNew(newsId, value)
    }
}
