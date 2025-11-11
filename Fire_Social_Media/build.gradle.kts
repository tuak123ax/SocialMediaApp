plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    id ("kotlin-android")
    id ("kotlin-kapt")
//    id ("dagger.hilt.android.plugin")
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.minhtu.firesocialmedia.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.minhtu.firesocialmedia"
        minSdk = 24
        targetSdk = 35
        versionCode = 500200
        versionName = "5.2.0"

        signingConfig = signingConfigs.getByName("debug")
    }
    signingConfigs{
        getByName("debug") {
            keyAlias = "test"
            keyPassword = "123456"
            storeFile = file("test.jks")
            storePassword = "123456"
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.activity.compose)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.navigation:navigation-compose:2.7.7") // Foundation library (for LazyRow, LazyColumn, etc.)
    implementation (libs.androidx.foundation)
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Force consistent Lifecycle version across all configurations to avoid corrupt resolution
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")
    androidTestImplementation("androidx.lifecycle:lifecycle-process:2.8.6")
    // Not a valid configuration, ensure androidTest uses the pinned version
    implementation("io.coil-kt:coil-compose:2.7.0")

    //Kotlin coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    //Encrypt data before saving to local
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("com.google.code.gson:gson:2.10.1")

    //Monitor memory leak
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")
}