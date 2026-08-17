package com.minhtu.firesocialmedia.network

sealed class DecentralizationType {
    object Public : DecentralizationType(){
        override fun toString(): String = "Public"
    }
    object Private : DecentralizationType(){
        override fun toString(): String = "Private"
    }
    object OnlyFriends : DecentralizationType(){
        override fun toString(): String = "OnlyFriends"
    }
}

