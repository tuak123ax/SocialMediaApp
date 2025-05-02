package com.minhtu.firesocialmedia.home.showimage

import android.content.Context
import androidx.lifecycle.ViewModel
import com.minhtu.firesocialmedia.services.database.DatabaseHelper

class ShowImageViewModel : ViewModel() {
    fun downloadImage(context : Context, image: String, fileName : String) {
        DatabaseHelper.downloadImage(context, image, fileName)
    }
}