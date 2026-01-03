package com.minhtu.firesocialmedia.data.remote.dto.group

import kotlinx.serialization.Serializable

@Serializable
data class GroupDTO(
    var id : String = "",
    var name : String = "",
    var avatar : String = "",
    var password : String = "",
    var createdDate : Long = 0,
    var members : HashMap<String, String> = HashMap(),
    var posts : ArrayList<String> = ArrayList()
)