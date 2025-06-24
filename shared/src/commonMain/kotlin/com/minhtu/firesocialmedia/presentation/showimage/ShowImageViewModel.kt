package com.minhtu.firesocialmedia.presentation.showimage

import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.showToast
import com.rickclephas.kmp.observableviewmodel.ViewModel

class ShowImageViewModel : ViewModel() {
    fun downloadImage(image: String, fileName : String, platform: PlatformContext) {
        platform.database.downloadImage(image, fileName, onComplete = {
            downloadImageTask ->
            if(downloadImageTask){
                showToast("Download Image Successfully!")
            } else {
                showToast("Cannot download image! Please try again.")
            }
        })
    }
}