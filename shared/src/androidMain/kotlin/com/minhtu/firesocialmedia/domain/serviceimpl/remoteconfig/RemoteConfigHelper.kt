package com.minhtu.firesocialmedia.domain.serviceimpl.remoteconfig

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class RemoteConfigHelper {

    companion object{
        fun getRemoteConfig() : FirebaseRemoteConfig {
            val remoteConfig = Firebase.remoteConfig
            updateRemoteConfigSettings(remoteConfig)
            return remoteConfig
        }
        fun updateRemoteConfigSettings(remoteConfig : FirebaseRemoteConfig){
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
        }
        fun fetchAndActiveConfig(remoteConfig : FirebaseRemoteConfig, callback: FetchResultCallback) {
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d("RemoteConfigHelper", "fetch success")
                    callback.fetchSuccess()
                } else {
                    Log.d("RemoteConfigHelper", "fetch fail")
                    callback.fetchFail()
                }
            }
        }
    }
}