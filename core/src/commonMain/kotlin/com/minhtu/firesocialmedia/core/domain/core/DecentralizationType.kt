package com.minhtu.firesocialmedia.core.domain.core

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