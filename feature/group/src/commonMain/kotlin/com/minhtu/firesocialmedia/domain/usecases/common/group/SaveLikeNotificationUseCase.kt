package com.minhtu.firesocialmedia.domain.usecases.common.group

import com.minhtu.firesocialmedia.group.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.group.entity.notification.NotificationType
import com.minhtu.firesocialmedia.group.entity.notification.toSharedNotification
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.group.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.group.platform.sendMessageToServer

class SaveLikeNotificationUseCase(
    private val getUserUseCase: GetUserUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase
) {
    suspend operator fun invoke(likerUser: UserInstance, posterId: String, newsId: String) {
        val notiContent = "${likerUser.name} liked your post!"
        val notification = NotificationInstance(
            id = getRandomIdForNotification(),
            content = notiContent,
            avatar = likerUser.image,
            sender = likerUser.uid,
            timeSend = getCurrentTime(),
            type = NotificationType.LIKE,
            relatedInfo = newsId
        )
        val poster = getUserUseCase.invoke(posterId, false)
        if (poster != null) {
            poster.addNotification(notification)
            saveNotificationToDatabaseUseCase.invoke(
                poster.uid,
                ArrayList(poster.notifications.map { it.toSharedNotification() })
            )
            val tokenList = arrayListOf(poster.token)
            sendMessageToServer(createMessageForServer(notiContent, tokenList, likerUser.token, likerUser.uid, likerUser.image, likerUser.email, likerUser.name, "BASIC"))
        }
    }
}
