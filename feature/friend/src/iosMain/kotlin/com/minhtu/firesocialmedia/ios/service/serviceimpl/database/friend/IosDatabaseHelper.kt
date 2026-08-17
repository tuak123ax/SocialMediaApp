package com.minhtu.firesocialmedia.ios.service.serviceimpl.database.friend

import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithDouble
import platform.Foundation.numberWithFloat
import platform.Foundation.numberWithInt
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosDatabaseHelper {
    companion object {
        private val database: FIRDatabaseReference = FIRDatabase.database().reference()

        suspend fun saveListToDatabase(
            id: String,
            path: String,
            value: List<String>,
            externalPath: String
        ) {
            try{
                val ref = database.child(path).child(id)
                    .let { if (externalPath.isNotEmpty()) it.child(externalPath) else it }

                setValue(ref, value)
            } catch (e : Exception){
                e.printStackTrace()
            }
        }

        private suspend fun setValue(ref: FIRDatabaseReference, value: Any): Boolean =
            suspendCancellableCoroutine { cont ->
                val preparedValue = prepareValueForFirebase(value)
                ref.setValue(preparedValue) { error, _ ->
                    if (error == null) cont.resume(true)
                    else cont.resumeWithException(Throwable(error.localizedDescription))
                }
            }

        @OptIn(BetaInteropApi::class)
        private fun prepareValueForFirebase(value: Any): Any {
            return when (value) {
                is Map<*, *> -> {
                    val dict = NSMutableDictionary()
                    value.forEach { (key, v) ->
                        if (key is String && v != null) {
                            dict.setObject(prepareValueForFirebase(v), key.toNSString())
                        }
                    }
                    dict
                }
                is List<*> -> {
                    val array = NSMutableArray()
                    value.forEach { item ->
                        if (item != null) {
                            array.addObject(prepareValueForFirebase(item))
                        }
                    }
                    array
                }
                is Boolean -> NSNumber.numberWithBool(value)
                is Int -> NSNumber.numberWithInt(value)
                is Double -> NSNumber.numberWithDouble(value)
                is Float -> NSNumber.numberWithFloat(value)
                is String -> NSString.create(string = value)
                else -> value
            }
        }

        @OptIn(BetaInteropApi::class)
        fun String.toNSString(): NSString = NSString.create(string = this)
    }
}
