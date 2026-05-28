plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)  // use same as :shared
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.kotlinCocoapods)
}

kotlin {
    androidTarget()
    jvmToolchain(17)

    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    cocoapods {
        summary = "Core module"
        homepage = "https://github.com/tuak123ax/SocialMediaApp"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../Fire_Social_Media/Podfile")
        framework {
            baseName = "core"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            // Add shared KMP deps here as needed
            //Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            api("io.github.qdsfdhvh:image-loader:1.10.0")
            // optional - Compose Multiplatform Resources Decoder
            api("io.github.qdsfdhvh:image-loader-extension-compose-resources:1.10.0")
            // optional - Moko Resources Decoder
            api("io.github.qdsfdhvh:image-loader-extension-moko-resources:1.10.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            // Android-specific deps
        }
        iosMain.dependencies {
            // iOS-specific deps
        }
    }
}

android {
        namespace = "com.minhtu.firesocialmedia.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}