package com.minhtu.firesocialmedia.domain.serviceimpl.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.minhtu.firesocialmedia.constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AndroidCryptoHelper {
    companion object{
        private const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"
        suspend fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
            return try {
                createEncryptedSharedPreferences(context)
            } catch (e: Exception) {
                Log.e("EncryptedPrefs", "Error in encrypted storage. Resetting...", e)
                resetEncryptedPreferences(context)
                delay(500) // Ensure reset is completed
                createEncryptedSharedPreferences(context)
            }
        }

        @Throws(Exception::class)
        private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                Log.e("EncryptedPrefs", "Master key missing. Recreating...")
                resetEncryptedPreferences(context)
            }

            val masterKey = MasterKey.Builder(context)
                .setKeyGenParameterSpec(
                    KeyGenParameterSpec.Builder(
                        MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                .build()

            return EncryptedSharedPreferences.create(
                context,
                "secure_prefs_file",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        private fun resetEncryptedPreferences(context: Context) {
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                    keyStore.deleteEntry(MASTER_KEY_ALIAS)
                    Log.e("EncryptedPrefs", "Keystore key deleted")
                }
            } catch (e: Exception) {
                Log.e("EncryptedPrefs", "Failed to delete keystore entry", e)
            }

            // Delete EncryptedSharedPreferences files
            val prefsDir = File(context.dataDir, "shared_prefs")
            val filesToDelete = prefsDir.listFiles()?.filter { it.name.contains("secure_prefs_file") } ?: emptyList()

            for (file in filesToDelete) {
                if (file.exists()) {
                    file.delete()
                    Log.e("EncryptedPrefs", "Deleted corrupted preferences file: ${file.name}")
                }
            }
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

        fun saveAccount(context: Context, email: String, password: String){
            CoroutineScope(Dispatchers.IO).launch {
                val secureSharedPreferences: SharedPreferences = getEncryptedSharedPreferences(context)
                secureSharedPreferences.edit().putString(Constants.KEY_EMAIL, email).apply()
                secureSharedPreferences.edit().putString(Constants.KEY_PASSWORD, password).apply()
            }
        }

        suspend fun clearAccount(context: Context){
            val secureSharedPreferences: SharedPreferences = getEncryptedSharedPreferences(context)
            secureSharedPreferences.edit().clear().apply()
        }
    }
}