package com.minhtu.firesocialmedia.group.platform

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
