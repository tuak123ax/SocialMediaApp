package com.minhtu.firesocialmedia.instance

import java.io.Serializable

data class NewsInstance(var newsId: String = "", var posterName: String = "", var avatar: String = "",
                        var message: String = "", var image: String = ""): Serializable{
                            fun updateNews(newsId: String, posterName: String, avatar: String,
                                           message: String, image: String){
                                this.newsId = newsId
                                this.posterName = posterName
                                this.avatar = avatar
                                this.message = message
                                this.image = image
                            }
                            fun updateImage(image: String) {
                                this.image = image
                            }
                        }