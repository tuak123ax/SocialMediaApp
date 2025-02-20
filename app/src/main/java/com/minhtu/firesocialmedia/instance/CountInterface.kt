package com.minhtu.firesocialmedia.instance

interface CountInterface {
    val likeCount : Int
    val commentCount : Int
    val timePosted : Long
    fun increaseLikeCount()
    fun increaseCommentCount()
    fun decreaseLikeCount()
    fun decreaseCommentCount()
}