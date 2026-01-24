package com.minhtu.firesocialmedia.domain.entity.group

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

data class GroupInstance(
    var id : String = "",
    var name : String = "",
    var avatar : String = "",
    var password : String = "",
    var description : String = "",
    var createdDate : Long = 0,
    var memberCount : Long = 0,
    var members : HashMap<String, String> = HashMap(),
    var posts: HashMap<String, NewsInstance> = HashMap()
)