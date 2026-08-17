package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO

fun interface FriendsLocalStore {
    suspend fun store(friends: List<UserDTO?>)
}
