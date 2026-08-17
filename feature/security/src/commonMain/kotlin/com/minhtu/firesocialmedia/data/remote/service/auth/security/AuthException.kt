package com.minhtu.firesocialmedia.data.remote.service.auth.security

class AuthException(val errorCode: String, message: String? = null) : Exception(message)
