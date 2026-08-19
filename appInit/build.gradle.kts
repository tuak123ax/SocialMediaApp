plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.ksp)
    //Room
    alias(libs.plugins.androidx.room)
}

kotlin {
    androidTarget()
    jvmToolchain(17)

    iosX64()
    iosArm64()
    iosSimulatorArm64("iosSimulator")

    // Link sqlite on Native/iOS (Room KMP support; iOS impl remains a no-op stub, see instruction.md)
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.all { linkerOpts("-lsqlite3") }
    }

    cocoapods {
        summary = "iOS Koin composition root for SocialMedia"
        homepage = "https://github.com/tuak123ax/SocialMediaApp"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../Fire_Social_Media/Podfile")
        framework {
            baseName = "AppKoin"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":feature:auth"))
            implementation(project(":feature:home"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:comment"))
            implementation(project(":feature:calling"))
            implementation(project(":feature:group"))
            implementation(project(":feature:security"))
            implementation(project(":feature:notification"))
            implementation(project(":feature:friend"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            //Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform("androidx.compose:compose-bom:2025.02.00"))
            implementation("androidx.compose.ui:ui")
            implementation("androidx.compose.material3:material3")
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            //Room
            implementation(libs.androidx.room.sqlite.wrapper)

            //Firebase (appInit's own getUser/searchUserByName, see UserDTO-out-of-core migration)
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.5.1"))
            implementation("com.google.firebase:firebase-database")
            implementation("com.google.firebase:firebase-auth")

            implementation("androidx.media3:media3-exoplayer:1.7.1")
            implementation("androidx.media3:media3-ui:1.7.1")
        }
        iosMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}

android {
    namespace = "com.minhtu.firesocialmedia.appinit"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
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
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
