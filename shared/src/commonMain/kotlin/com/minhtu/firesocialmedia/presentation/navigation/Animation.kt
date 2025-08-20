package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

typealias Enter = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
typealias Exit  = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?

object DefaultNavAnimations {
    val enter: Enter = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(700)
        )
    }

    val popEnter: Enter = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(700)
        )
    }

    val exit: Exit = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(700)
        )
    }

    val popExit: Exit = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(700)
        )
    }
}