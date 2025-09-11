package com.minhtu.firesocialmedia.domain.entity.base

interface CountInterface {
    val likeCount : Int
    val commentCount : Int
    val timePosted : Long
    fun increaseLikeCount()
    fun increaseCommentCount()
    fun decreaseLikeCount()
    fun decreaseCommentCount()
}