package com.minhtu.firesocialmedia.presentation.showimage

import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.usecases.showimage.DownloadImageUseCase
import com.minhtu.firesocialmedia.platform.showToast
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ShowImageViewModel(
    private val downloadImageUseCase : DownloadImageUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun downloadImage(image: String, fileName : String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val result = downloadImageUseCase.invoke(image, fileName)
                if(result) {
                    showToast("Download Image Successfully!")
                } else {
                    showToast("Cannot download image! Please try again.")
                }
            }
        }
    }
}