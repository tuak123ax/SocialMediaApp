import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.compose")
//    id("com.google.dagger.hilt.android") //  Added Hilt plugin
//    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget()
    jvmToolchain(17)  //  Use standard way to set Java 17

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Shared module for iOS and Android"
        homepage = "https://github.com/tuak123ax/SocialMediaApp"
        version = "2.0"
        ios.deploymentTarget = "13.0"
        podfile = project.file("../Fire_Social_Media/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ui)
                implementation(libs.material3)
                implementation(libs.foundation)
                implementation(libs.runtime)
                implementation(compose.components.resources)
            }
        }
    }
}

android {
    namespace = "com.minhtu.firesocialmedia"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.foundation)

    //  Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("io.coil-kt:coil-compose:2.7.0")

    //  Kotlin Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    //  Encrypt data
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    //  Hilt
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") //  Hilt for Jetpack Compose
//    implementation("com.google.dagger:hilt-android:2.51.1") //  Core Hilt
//    kapt("com.google.dagger:hilt-compiler:2.51.1") // Hilt Annotation Processor

    implementation("androidx.compose.runtime:runtime:1.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
}