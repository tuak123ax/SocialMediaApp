package com.minhtu.firesocialmedia.domain.usecases.news.profile

import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository
import com.minhtu.firesocialmedia.profile.entity.news.ProfileNewsPage

class GetNewsByUserUseCase(
    private val profileNewsRepository: ProfileNewsRepository
) {
    suspend operator fun invoke(
        posterId: String,
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?
    ): ProfileNewsPage {
        return profileNewsRepository.getNewsByUser(posterId, number, lastTimePosted, lastKey)
    }
}
