package com.minhtu.firesocialmedia.android.service.serviceimpl.crypto

import android.content.Context
import com.minhtu.firesocialmedia.constants.profile.Constants
import com.minhtu.firesocialmedia.data.local.service.crypto.ProfileCryptoService
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO

class AndroidProfileCryptoService(private val context: Context) : ProfileCryptoService {
    override suspend fun getCurrentUserInfo(): UserDTO? {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(context)
        val image = secureSharedPreferences.getString(Constants.KEY_AVATAR, "")
        val name = secureSharedPreferences.getString(Constants.KEY_NAME, "")
        val status = secureSharedPreferences.getString(Constants.KEY_STATUS, "")
        val token = secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")
        val uid = secureSharedPreferences.getString(Constants.KEY_USER_ID, "")
        val friends = secureSharedPreferences.getStringSet(Constants.KEY_FRIENDS, emptySet())
        val friendRequests =
            secureSharedPreferences.getStringSet(Constants.KEY_FRIEND_REQUEST, emptySet())
        return if (!uid.isNullOrEmpty() &&
            !name.isNullOrEmpty() &&
            !image.isNullOrEmpty() &&
            status != null && token != null && friends != null && friendRequests != null
        ) {
            UserDTO(
                image = image,
                name = name,
                status = status,
                token = token,
                uid = uid,
                friends = friends.toCollection(ArrayList()),
                friendRequests = friendRequests.toCollection(ArrayList())
            )
        } else {
            null
        }
    }
}
