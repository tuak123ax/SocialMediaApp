package com.minhtu.firesocialmedia.instance

import java.io.Serializable

data class UserInstance(var email: String = "", var image: String = "", var name: String = "",
                        var status: String = "", var token: String = "", var uid: String = ""):Serializable
{
    fun updateInformation(email: String, image: String, name: String,
                           status: String, token: String, uid: String){
        this.email = email
        this.image = image
        this.name = name
        this.status = status
        this.token = token
        this.uid = uid
    }
    fun updateImage(image: String){
        this.image = image
    }
}
