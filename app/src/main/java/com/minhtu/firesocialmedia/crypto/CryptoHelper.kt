package com.minhtu.firesocialmedia.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.minhtu.firesocialmedia.constants.Constants
import java.io.File
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoHelper {
    companion object{
        fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
            if (!isKeyValid("androidx.security.master_key_default")) {
                Log.e("EncryptedPrefs", "Keystore key missing or invalid. Resetting preferences...")
                resetEncryptedPreferences(context)
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            }

            val prefs = try {
                createEncryptedSharedPreferences(context)
            } catch (e: AEADBadTagException) {
                Log.e("EncryptedPrefs", "AEADBadTagException: Corrupted encrypted storage detected. Resetting...")
                resetEncryptedPreferences(context)
                createEncryptedSharedPreferences(context)
            } catch (e: Exception) {
                Log.e("EncryptedPrefs", "Unknown error in encrypted storage", e)
                resetEncryptedPreferences(context)
                createEncryptedSharedPreferences(context)
            }

            if (!prefs.contains("initialized")) {
                prefs.edit().putBoolean("initialized", true).apply()
            }

            return prefs
        }


        private fun createEncryptedSharedPreferences(context: Context) : SharedPreferences{
            var sharedPrefs: SharedPreferences? = null
            var attempts = 0

            while (sharedPrefs == null && attempts < 3) {
                try {
                    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                    sharedPrefs = EncryptedSharedPreferences.create(
                        "account_secure_prefs",
                        masterKeyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                } catch (e: Exception) {
                    Log.e("EncryptedPrefs", "Keystore error, retrying... Attempt: ${attempts + 1}")
                    Thread.sleep(500) // Wait 500ms before retrying
                    attempts++
                }
            }

            return sharedPrefs ?: throw RuntimeException("Failed to create EncryptedSharedPreferences after multiple attempts")
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

        private fun resetEncryptedPreferences(context: Context) {
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                keyStore.deleteEntry("androidx.security.master_key_default")
            } catch (e: Exception) {
                Log.e("EncryptedPrefs", "Failed to delete keystore entry", e)
            }

            val sharedPrefsFile = File(context.filesDir, "shared_prefs/account_secure_prefs.xml")
            if (sharedPrefsFile.exists()) {
                sharedPrefsFile.delete()
                Log.e("EncryptedPrefs", "Deleted corrupted preferences file.")
            }
        }
        fun isKeyValid(alias: String): Boolean {
            return try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                val key = keyStore.getKey(alias, null) as? SecretKey

                if (key == null) {
                    Log.e("EncryptedPrefs", "Keystore key is missing")
                    false
                } else {
                    // Ensure the key works (only if it exists)
                    try {
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_SIZE, ByteArray(GCM_IV_SIZE)))
                        true
                    } catch (e: Exception) {
                        Log.e("EncryptedPrefs", "Keystore key exists but is invalid", e)
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e("EncryptedPrefs", "Keystore access failed", e)
                false
            }
        }

        fun saveAccount(context: Context, email: String, password: String){
            val secureSharedPreferences: SharedPreferences = getEncryptedSharedPreferences(context)
            secureSharedPreferences.edit().putString(Constants.KEY_EMAIL, email).apply()
            secureSharedPreferences.edit().putString(Constants.KEY_PASSWORD, password).apply()
        }

        fun clearAccount(context: Context){
            val secureSharedPreferences: SharedPreferences = getEncryptedSharedPreferences(context)
            secureSharedPreferences.edit().clear().apply()
        }
    }
}