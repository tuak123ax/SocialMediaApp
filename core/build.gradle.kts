import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)  // use same as :shared
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.kotlinCocoapods)
    
    id("org.jetbrains.kotlinx.kover")
    id("io.mockative") version "3.0.1"
    alias(libs.plugins.ksp)
    id("kotlinx-serialization")
    //Room
    alias(libs.plugins.androidx.room)
}

kotlin {
    androidTarget()
    jvmToolchain(17)

    iosX64()
    iosArm64()
    iosSimulatorArm64("iosSimulator")

    // Link sqlite on Native/iOS
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.all { linkerOpts("-lsqlite3") }
    }


    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        binaries.all {
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.minhtu.firesocialmedia.core")
        }
    }

    cocoapods {
        summary = "Core module for iOS and Android"
        homepage = "https://github.com/tuak123ax/SocialMediaApp"
        version = "2.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../Fire_Social_Media/Podfile")
        // Use direct pod names to generate correct cinterop modules
        pod("FirebaseAuth") {
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("FirebaseDatabase") {
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("FirebaseStorage") {
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("FirebaseMessaging") {
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        framework {
            baseName = "core"
            isStatic = true
        }
    }
    val kotlinVersion = "1.7.3"

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")
            implementation("org.jetbrains.compose.components:components-resources:$kotlinVersion")
            api("com.rickclephas.kmp:kmp-observableviewmodel-core:1.0.0-BETA-10")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

            implementation("org.jetbrains.compose.material:material-icons-extended:$kotlinVersion")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")

            implementation("org.javassist:javassist:3.29.2-GA")
            implementation("org.objenesis:objenesis:3.3")

            //Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            //QR
            implementation("io.github.g0dkar:qrcode-kotlin:4.5.0")

            //Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            //Di
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            //Image loader
            api(libs.seiko.image.loader)
        }
        commonTest {
            dependencies{
                implementation(kotlin("test"))
                implementation("io.mockative:mockative:3.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinVersion")
            }
        }
        androidMain.dependencies {
            implementation(platform("androidx.compose:compose-bom:2025.02.00"))
            implementation("androidx.compose.ui:ui")
            implementation("androidx.compose.ui:ui-tooling-preview")
            implementation("androidx.compose.material3:material3")
            implementation(libs.androidx.activity.compose)
            implementation("androidx.core:core-ktx:1.13.1")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
            implementation("androidx.lifecycle:lifecycle-process:2.9.0")
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
            implementation("androidx.compose.foundation:foundation")

            //  Firebase BoM
            implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
            implementation("com.google.firebase:firebase-analytics")
            implementation("com.google.firebase:firebase-crashlytics")
            implementation("com.google.firebase:firebase-messaging")
            implementation("com.google.firebase:firebase-database")
            implementation("com.google.firebase:firebase-storage")
            implementation("com.google.firebase:firebase-config")

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

            // Use BOM-managed Compose versions

            implementation("com.squareup.retrofit2:retrofit:2.9.0")
            implementation("com.squareup.retrofit2:converter-gson:2.9.0")
            implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

            implementation(libs.ktor.client.okhttp)

            implementation("androidx.media3:media3-exoplayer:1.7.1")
            implementation("androidx.media3:media3-ui:1.7.1")

            //Room
            implementation(libs.androidx.room.sqlite.wrapper)

            //Di
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
        }

        androidUnitTest.dependencies {
            implementation("io.mockk:mockk:1.14.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinVersion")
            implementation("junit:junit:4.13.2")
        }

        androidInstrumentedTest.dependencies {
            implementation("androidx.test:core-ktx:1.6.1")
            implementation("androidx.test.ext:junit:1.2.1")
            implementation("androidx.test.espresso:espresso-core:3.6.1")
            implementation("androidx.compose.ui:ui-test-junit4")
        }

        iosMain.dependencies {
            api(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-native-mt")
            implementation(libs.ktor.client.darwin)
            implementation(libs.seiko.image.loader)
            implementation("com.squareup.okio:okio:3.9.0")
        }
    }
}

ksp {
    arg("mockative.generateMocksForDefaultArguments", "true")
}

android {
    namespace = "com.minhtu.firesocialmedia.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField(
            "String",
            "APP_SCRIPT_FOR_2FA_AUTHENTICATION_API_KEY",
            "\"${localProperties.getProperty("APP_SCRIPT_FOR_2FA_AUTHENTICATION_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_API_KEY",
            "\"${localProperties.getProperty("SUPABASE_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "IPINFO_API_KEY",
            "\"${localProperties.getProperty("IPINFO_API_KEY", "")}\""
        )
    }
    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
}

room {
    schemaDirectory("$projectDir/schemas")
}

