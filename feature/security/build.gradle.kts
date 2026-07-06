plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget()
    jvmToolchain(17)

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        val isSimulator = name.contains("Simulator", ignoreCase = true) || name.contains("X64", ignoreCase = true)
        val buildVariant = if (isSimulator) "Debug-iphonesimulator" else "Debug-iphoneos"
        val podBuildSuffix = if (isSimulator) "IosSimulator" else "Ios"
        val firebaseFrameworksDir = rootProject.layout.projectDirectory.dir("core/build/cocoapods/synthetic/ios/build/$buildVariant").asFile
        val firebaseFrameworkSearchPaths = listOf(
            firebaseFrameworksDir.absolutePath,
            "${firebaseFrameworksDir.absolutePath}/FirebaseAppCheckInterop",
            "${firebaseFrameworksDir.absolutePath}/FirebaseAuth",
            "${firebaseFrameworksDir.absolutePath}/FirebaseAuthInterop",
            "${firebaseFrameworksDir.absolutePath}/FirebaseCore",
            "${firebaseFrameworksDir.absolutePath}/FirebaseCoreExtension",
            "${firebaseFrameworksDir.absolutePath}/FirebaseCoreInternal",
            "${firebaseFrameworksDir.absolutePath}/FirebaseDatabase",
            "${firebaseFrameworksDir.absolutePath}/FirebaseInstallations",
            "${firebaseFrameworksDir.absolutePath}/FirebaseMessaging",
            "${firebaseFrameworksDir.absolutePath}/FirebaseSharedSwift",
            "${firebaseFrameworksDir.absolutePath}/FirebaseStorage",
            "${firebaseFrameworksDir.absolutePath}/GTMSessionFetcher",
            "${firebaseFrameworksDir.absolutePath}/GoogleDataTransport",
            "${firebaseFrameworksDir.absolutePath}/GoogleUtilities",
            "${firebaseFrameworksDir.absolutePath}/PromisesObjC",
            "${firebaseFrameworksDir.absolutePath}/RecaptchaInterop",
            "${firebaseFrameworksDir.absolutePath}/leveldb-library",
            "${firebaseFrameworksDir.absolutePath}/nanopb"
        )
        val firebasePodBuildTasks = listOf(
            ":core:podInstallSyntheticIos",
            ":core:podBuildFirebaseAuth$podBuildSuffix",
            ":core:podBuildFirebaseDatabase$podBuildSuffix",
            ":core:podBuildFirebaseMessaging$podBuildSuffix",
            ":core:podBuildFirebaseStorage$podBuildSuffix"
        )

        binaries.all {
            firebaseFrameworkSearchPaths.forEach {
                linkerOpts("-F$it")
                linkerOpts("-rpath", it)
            }
            linkTaskProvider.configure {
                dependsOn(firebasePodBuildTasks)
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        }
        commonMain {
            kotlin.srcDir("src/commonMain/kotlin/com/minhtu/firesocialmedia/domain")
            kotlin.srcDir("src/commonMain/kotlin/com/minhtu/firesocialmedia/di")
            kotlin.exclude("com/minhtu/firesocialmedia/core/domain/**")
            kotlin.exclude("com/minhtu/firesocialmedia/security/domain/**")
            kotlin.exclude("com/minhtu/firesocialmedia/security/di/**")
        }
        commonMain.dependencies {
            implementation(project(":core"))
            

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")

            api("com.rickclephas.kmp:kmp-observableviewmodel-core:1.0.0-BETA-10")

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
        }
    }
}

android {
    namespace = "com.minhtu.firesocialmedia.security"
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

