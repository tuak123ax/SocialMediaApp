package com.minhtu.firesocialmedia.home.entity.comment.base

/**
 * Feature-home-owned clone of feature/comment's `domain.entity.base.CountInterface`.
 */
interface CountInterface {
    val likeCount : Int
    val commentCount : Int
    val timePosted : Long
    fun increaseLikeCount()
    fun increaseCommentCount()
    fun decreaseLikeCount()
    fun decreaseCommentCount()
}
