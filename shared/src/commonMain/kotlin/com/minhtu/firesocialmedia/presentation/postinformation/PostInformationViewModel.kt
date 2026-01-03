package com.minhtu.firesocialmedia.presentation.postinformation

import androidx.compose.runtime.mutableStateOf
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.usecases.notification.FindNewByIdInDbUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PostInformationViewModel(
    private val findNewByIdInDbUseCase: FindNewByIdInDbUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _newFromDeepLink = MutableStateFlow<NewsInstance?>(null)
    var newFromDeepLink = _newFromDeepLink.asStateFlow()
    suspend fun requestFindNewById(newId: String) {
        _newFromDeepLink.value = findNewByIdInDbUseCase.invoke(newId)
    }

    private var _sharedNew = MutableStateFlow<NewsInstance?>(null)
    var sharedNew = _sharedNew.asStateFlow()
    fun updateShareNew(new : NewsInstance) {
        _sharedNew.value = new
    }

    fun resetShareNew() {
        _sharedNew.value = null
    }
    fun getSharedNew(sharedNewId : String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                _sharedNew.value = findNewByIdInDbUseCase.invoke(sharedNewId)
            }
        }
    }
}