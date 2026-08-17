package com.minhtu.firesocialmedia.domain.usecases.news.profile

import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance

class DeleteNewsUseCase(
    private val profileNewsRepository: ProfileNewsRepository
) {
    suspend operator fun invoke(new: NewsInstance): Boolean {
        return profileNewsRepository.deleteNewsFromDatabase(new)
    }
}
