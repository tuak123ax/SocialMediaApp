package com.minhtu.firesocialmedia.search.entity.news

import com.minhtu.firesocialmedia.network.DecentralizationType

data class NewsInstance(var id: String = "",
                        var posterId : String = "",
                        var posterName: String = "",
                        var avatar: String = "",
                        var message: String = "",
                        var image: String = "",
                        var video: String = "",
                        var isVisible: Boolean = true,
                        var likeCount: Int = 0,
                        var commentCount: Int = 0,
                        var timePosted: Long = 0,
                        var localPath : String = "",
                        var shareContentId : String = "",
                        var decentralizationType : DecentralizationType? = null,
                        var groupId : String = "",
                        var type: String? = null,
                        var pollId: String? = null)
