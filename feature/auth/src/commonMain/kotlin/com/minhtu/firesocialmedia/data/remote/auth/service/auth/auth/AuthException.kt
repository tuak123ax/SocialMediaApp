package com.minhtu.firesocialmedia.data.remote.auth.service.auth.auth

class AuthException(val errorCode: String, message: String? = null) : Exception(message)
