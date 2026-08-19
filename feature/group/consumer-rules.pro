# Consumer ProGuard rules for the :feature:group library module.
# These rules are automatically merged into the final app's R8/ProGuard
# configuration via consumerProguardFiles in build.gradle.kts.

############################################
# feature:group – ViewModels
#
# All group ViewModels are resolved by Koin at runtime using KClass tokens.
# R8 must not rename or remove them, and their constructor parameter types
# must be kept so Koin can inject dependencies.
############################################
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.group.presentation.**.*ViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.group.presentation.**.*ViewModel {
    public <init>(...);
}

############################################
# feature:group – DI module
#
# GroupModule.kt and the top-level groupModule() function must survive R8
# so that Koin can load the module at startup.
############################################
-keep class com.minhtu.firesocialmedia.feature.group.di.** { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.group.di.** { *; }

############################################
# feature:group – Navigation
#
# GroupNavGraph interface and GroupNavGraphImpl are resolved by Koin
# (single<GroupNavGraph> { GroupNavGraphImpl() }) and called from the
# shared Navigation.kt via koinInject(). Their names must be preserved.
############################################
-keep interface com.minhtu.firesocialmedia.feature.group.navigation.** { *; }
-keep class com.minhtu.firesocialmedia.feature.group.navigation.** { *; }

############################################
# feature:group – Composable screens
#
# Each screen is a @Composable function compiled into a file-facade class.
# koinViewModel<T>() call sites inside these facades must keep their KClass
# tokens intact after R8 inlines the reified function.
############################################
-keep class com.minhtu.firesocialmedia.feature.group.presentation.** { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.group.presentation.** { *; }

############################################
# PollViewModelInterface
#
# Defined in :shared and cast to in UiUtils.kt at runtime.
# Its generic StateFlow return types must survive R8 type-erasure.
############################################
-keep interface com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.PollViewModelInterface { *; }

