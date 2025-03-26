package com.minhtu.firesocialmedia.instance

import java.io.Serializable

data class NotificationInstance(
    val title:String = "",
    val body : String ="",
    val avatar : String ="",
    val sender : String = ""
):Serializable
{

}
