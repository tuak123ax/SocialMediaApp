package com.minhtu.firesocialmedia.instance

interface BaseNewsInstance {
    val id: String
    val posterId : String
    val posterName: String
    val avatar: String
    val message: String
    val image: String
    fun updateImage(image : String)
}