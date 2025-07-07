import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.compose") version "1.7.3"
//    id("com.google.dagger.hilt.android") //  Added Hilt plugin
//    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")

    id("org.jetbrains.kotlinx.kover")
    id("io.mockative") version "3.0.1"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
}

kotlin {
    androidTarget()
    jvmToolchain(21)

    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64("iosSimulator")

    val xcf = XCFramework()

    iosX64.binaries.framework {
        baseName = "shared"
        xcf.add(this)
        freeCompilerArgs += listOf("-Xbinary=bundleId=com.minhtu.firesocialmedia.shared")
    }

    iosArm64.binaries.framework {
        baseName = "shared"
        xcf.add(this)
        freeCompilerArgs += listOf("-Xbinary=bundleId=com.minhtu.firesocialmedia.shared")
    }

    iosSimulatorArm64.binaries.framework {
        baseName = "shared"
        xcf.add(this)
        freeCompilerArgs += listOf("-Xbinary=bundleId=com.minhtu.firesocialmedia.shared")
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        binaries.all {
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.minhtu.firesocialmedia.shared")
        }
    }

    cocoapods {
        summary = "Shared module for iOS and Android"
        homepage = "https://github.com/tuak123ax/SocialMediaApp"
        version = "2.0"
        ios.deploymentTarget = "13.0"
        podfile = project.file("../Fire_Social_Media/Podfile")
        pod("FirebaseAuth"){
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("FirebaseDatabase"){
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("FirebaseStorage"){
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("FirebaseMessaging"){
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        framework {
            baseName = "shared"
            isStatic = false
        }
    }

    val ktorVersion = "3.0.3"

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("org.jetbrains.compose.components:components-resources:1.7.3")
            api("com.rickclephas.kmp:kmp-observableviewmodel-core:1.0.0-BETA-10")
            implementation("io.ktor:ktor-client-core:$ktorVersion")

            api("io.github.qdsfdhvh:image-loader:1.10.0")
            // optional - Compose Multiplatform Resources Decoder
            api("io.github.qdsfdhvh:image-loader-extension-compose-resources:1.10.0")
            // optional - Moko Resources Decoder
            api("io.github.qdsfdhvh:image-loader-extension-moko-resources:1.10.0")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

            implementation("org.jetbrains.compose.material:material-icons-extended:1.5.10")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")

            implementation("org.javassist:javassist:3.29.2-GA")
            implementation("org.objenesis:objenesis:3.3")
        }
        commonTest {
            dependencies{
                implementation(kotlin("test"))
                implementation("io.mockative:mockative:3.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        androidMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.material3)
            implementation(libs.androidx.activity.compose)
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
            implementation("io.coil-kt.coil3:coil-compose:3.1.0")
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

            //  Kotlin Coroutines
            implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

            //  Encrypt data
            implementation ("androidx.security:security-crypto:1.1.0-alpha06")

            //  Hilt
//            implementation("androidx.hilt:hilt-navigation-compose:1.2.0") //  Hilt for Jetpack Compose
//    implementation("com.google.dagger:hilt-android:2.51.1") //  Core Hilt
//    kapt("com.google.dagger:hilt-compiler:2.51.1") // Hilt Annotation Processor

            implementation("androidx.compose.runtime:runtime:1.6.0")
            implementation("androidx.compose.foundation:foundation:1.6.0")
            implementation("androidx.compose.material3:material3:1.2.0")

            implementation("com.squareup.retrofit2:retrofit:2.9.0")
            implementation("com.squareup.retrofit2:converter-gson:2.9.0")
            implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

            implementation("androidx.media3:media3-exoplayer:1.7.1")
            implementation("androidx.media3:media3-ui:1.7.1")

            //webRTC
            implementation("io.getstream:stream-webrtc-android:1.3.8")
        }

        androidUnitTest.dependencies {
            implementation("io.mockk:mockk:1.14.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            implementation("junit:junit:4.13.2")
        }

        iosMain.dependencies {
            api(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-native-mt")

            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion") // Optional for logs
        }
    }
}

ksp {
    arg("mockative.generateMocksForDefaultArguments", "true")
}

dependencies {
    add("kspCommonMainMetadata", "io.mockative:mockative-processor:3.0.1")
}

android {
    namespace = "com.minhtu.firesocialmedia"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}
