package com.minhtu.firesocialmedia.domain.usecases.newsfeed.group

import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository

class LoadAllVotersUseCase(private val newsRepository: GroupNewsRepository) {
    suspend operator fun invoke(pollId: String): Map<String, List<Int>> {
        return newsRepository.loadAllVoters(pollId)
    }
}
