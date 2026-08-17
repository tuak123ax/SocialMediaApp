package com.minhtu.firesocialmedia.profile.data.remote.dto.news

import kotlinx.serialization.Serializable

@Serializable
data class NewsDTO(var id: String = "",
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
                    var decentralizationType : String = "",
                    var groupId : String = "",
                    var type: String? = null,
                    var pollId: String? = null)
