package com.minhtu.firesocialmedia.domain.usecases.news.profile

import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository

class UpdateLikeCountForNewUseCase(
    private val profileNewsRepository: ProfileNewsRepository
) {
    suspend operator fun invoke(newsId: String, value: Int) {
        profileNewsRepository.updateLikeCountForNew(newsId, value)
    }
}
