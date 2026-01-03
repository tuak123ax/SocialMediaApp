package com.minhtu.firesocialmedia.domain.entity.group

data class GroupInstance(
    var id : String = "",
    var name : String = "",
    var avatar : String = "",
    var password : String = "",
    var createdDate : Long = 0,
    var members : HashMap<String, String> = HashMap(),
    var posts : ArrayList<String> = ArrayList()
)