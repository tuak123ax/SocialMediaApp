package com.minhtu.firesocialmedia.comment.platform

expect fun createMessageForServer(
    message: String,
    tokenList: ArrayList<String>,
    senderToken: String,
    senderUid: String,
    senderImage: String,
    senderEmail: String,
    senderName: String,
    type: String
): String

expect fun sendMessageToServer(request: String)
