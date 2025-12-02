package com.minhtu.firesocialmedia.presentation.postinformation

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.usecases.notification.FindNewByIdInDbUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostInformationViewModel(
    private val findNewByIdInDbUseCase: FindNewByIdInDbUseCase
) : ViewModel() {
    private val _newFromDeepLink = MutableStateFlow<NewsInstance?>(null)
    var newFromDeepLink = _newFromDeepLink.asStateFlow()
    suspend fun requestFindNewById(newId: String) {
        _newFromDeepLink.value = findNewByIdInDbUseCase.invoke(newId)
    }
}