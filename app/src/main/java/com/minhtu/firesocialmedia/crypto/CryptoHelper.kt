package com.minhtu.firesocialmedia.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

class CryptoHelper {
    companion object{
        fun getEncryptedSharedPreferences(context: Context) : SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                "account_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        }
        const val GCM_IV_SIZE = 12   // IV size in bytes (96 bits)
        const val GCM_TAG_SIZE = 128 // Authentication tag size in bits

        fun encryptAESGCM(data: ByteArray, secretKeyString: String, ivString : String): ByteArray {
            val secretKeyByte = android.util.Base64.decode(secretKeyString, 0)
            // Create a SecretKey object from the byte array
            val secretKey = SecretKeySpec(secretKeyByte, 0, secretKeyByte.size, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = android.util.Base64.decode(ivString, 0)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_SIZE, iv))
            val encryptedData = cipher.doFinal(data)
            return iv + encryptedData // Prepend IV for decryption
        }

        fun decryptAESGCM(encryptedData: ByteArray, secretKeyString: String): ByteArray {
            val secretKeyByte = android.util.Base64.decode(secretKeyString, 0)
            // Create a SecretKey object from the byte array
            val secretKey = SecretKeySpec(secretKeyByte, 0, secretKeyByte.size, "AES")
            val iv = encryptedData.copyOfRange(0, GCM_IV_SIZE) // Extract IV
            val cipherText = encryptedData.copyOfRange(GCM_IV_SIZE, encryptedData.size) // Extract ciphertext
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_SIZE, iv))
            return cipher.doFinal(cipherText)
        }
    }
}