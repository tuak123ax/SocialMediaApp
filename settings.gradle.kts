enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "SocialMedia"
include(":Fire_Social_Media")
include(":shared")

include(":core")
include(":feature:auth")
project(":feature:auth").projectDir = file("feature/auth")
include(":feature:home")
project(":feature:home").projectDir = file("feature/home")
include(":feature:profile")
project(":feature:profile").projectDir = file("feature/profile")
include(":feature:calling")
project(":feature:calling").projectDir = file("feature/calling")
include(":feature:groupfeature")
project(":feature:groupfeature").projectDir = file("feature/group")
