package com.minhtu.firesocialmedia.android.service.serviceimpl.crypto

import android.content.Context
import com.minhtu.firesocialmedia.constants.home.Constants
import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO

class AndroidHomeCryptoService(private val context: Context) : HomeCryptoService {
    override suspend fun clearAccount() {
        AndroidCryptoHelper.clearAccount(context)
    }

    override suspend fun saveCurrentUserInfo(user: UserDTO) {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(context)
        secureSharedPreferences.edit().putString(Constants.KEY_AVATAR, user.image).apply()
        secureSharedPreferences.edit().putString(Constants.KEY_NAME, user.name).apply()
        secureSharedPreferences.edit().putString(Constants.KEY_STATUS, user.status).apply()
        secureSharedPreferences.edit().putString(Constants.KEY_FCM_TOKEN, user.token).apply()
        secureSharedPreferences.edit().putString(Constants.KEY_USER_ID, user.uid).apply()
        secureSharedPreferences.edit().putStringSet(Constants.KEY_FRIENDS, user.friends.toSet())
            .apply()
        secureSharedPreferences.edit()
            .putStringSet(Constants.KEY_FRIEND_REQUEST, user.friendRequests.toSet()).apply()
    }
}
