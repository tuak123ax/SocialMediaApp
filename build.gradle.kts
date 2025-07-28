plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinCocoapods).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false

    id("com.google.dagger.hilt.android") version "2.52" apply false
    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    id("org.jetbrains.compose") version "1.7.3" apply false
    id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-42"
    kotlin("plugin.serialization") version "2.1.10"

    //Code coverage tool
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
}
