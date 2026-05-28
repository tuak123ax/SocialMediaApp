package com.minhtu.firesocialmedia.core.domain.usecases.showimage

import com.minhtu.firesocialmedia.core.domain.repository.ShowImageRepository

class DownloadImageUseCase(
    private val showImageRepository : ShowImageRepository
) {
    suspend operator fun invoke(image: String, fileName: String) : Boolean{
        return showImageRepository.downloadImage(image, fileName)
    }
}