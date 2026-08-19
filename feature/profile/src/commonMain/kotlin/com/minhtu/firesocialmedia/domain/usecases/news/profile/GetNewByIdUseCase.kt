package com.minhtu.firesocialmedia.domain.usecases.news.profile

import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance

class GetNewByIdUseCase(
    private val profileNewsRepository: ProfileNewsRepository
) {
    suspend operator fun invoke(newId: String): NewsInstance? {
        return profileNewsRepository.getNew(newId)
    }
}
