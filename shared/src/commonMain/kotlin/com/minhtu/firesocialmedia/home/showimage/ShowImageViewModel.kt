package com.minhtu.firesocialmedia.home.showimage

import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.showToast
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