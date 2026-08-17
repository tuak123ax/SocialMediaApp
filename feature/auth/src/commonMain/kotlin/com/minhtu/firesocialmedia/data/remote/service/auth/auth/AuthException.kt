package com.minhtu.firesocialmedia.data.remote.service.auth.auth

class AuthException(val errorCode: String, message: String? = null) : Exception(message)
