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
        val firebaseFrameworksDir = rootProject.layout.projectDirectory.dir("shared/build/cocoapods/synthetic/ios/build/$buildVariant").asFile
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
            ":shared:podInstallSyntheticIos",
            ":shared:podBuildFirebaseAuth$podBuildSuffix",
            ":shared:podBuildFirebaseDatabase$podBuildSuffix",
            ":shared:podBuildFirebaseMessaging$podBuildSuffix",
            ":shared:podBuildFirebaseStorage$podBuildSuffix"
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
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":shared"))
            implementation(project(":feature:auth"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")

            api("com.rickclephas.kmp:kmp-observableviewmodel-core:1.0.0-BETA-10")

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Image loader
            api(libs.seiko.image.loader)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
        }
    }
}

android {
    namespace = "com.minhtu.firesocialmedia.feature.profile"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

