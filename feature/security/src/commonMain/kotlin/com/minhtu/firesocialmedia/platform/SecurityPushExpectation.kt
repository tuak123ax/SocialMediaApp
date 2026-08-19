package com.minhtu.firesocialmedia.security.platform

expect fun createCallMessage(
    message: String,
    tokenList: ArrayList<String>,
    sessionId: String,
    senderUid: String,
    senderName: String,
    senderImage: String,
    receiverUid: String,
    receiverName: String,
    receiverImage: String,
    type: String
): String

expect fun sendMessageToServer(request: String)
