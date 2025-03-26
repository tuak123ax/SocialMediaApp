package com.minhtu.firesocialmedia.instance

import java.io.Serializable

data class UserInstance(var email: String = "", var image: String = "", var name: String = "",
                        var status: String = "", var token: String = "", var uid: String = "",
                        var likedPosts: HashMap<String,Boolean> = HashMap()
):Serializable
{
    val notifications : ArrayList<NotificationInstance> = ArrayList()
    fun updateImage(image: String){
        this.image = image
    }
}
