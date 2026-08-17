# Consumer ProGuard rules for the :appInit module.
# appInit is the Koin composition root: it hosts the app's own ViewModels,
# its top-level DI module list, and the NavGraph implementations that wire
# every feature module together. These rules are merged into the app's R8
# config through consumerProguardFiles.

# Keep appInit's own ViewModels and constructor type metadata for Koin injection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.SearchViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.loading.SyncLoadingViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.navigation.RouterViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.search.SessionViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.presentation.SearchViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.loading.SyncLoadingViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.navigation.RouterViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.search.SessionViewModel {
    public <init>(...);
}

# Keep the top-level Koin module declarations loaded at app start.
-keep class com.minhtu.firesocialmedia.di.AppInitKoinModulesKt { *; }
-keep class com.minhtu.firesocialmedia.di.SearchModuleKt { *; }
-keep class com.minhtu.firesocialmedia.di.AppInitAndroidModuleKt { *; }

# Keep every NavGraph implementation resolved via Koin (single<XNavGraph> { XNavGraphImpl() })
# and invoked from Navigation.kt through koinInject(). Class names must be preserved.
-keep class com.minhtu.firesocialmedia.navigation.** { *; }
-keep interface com.minhtu.firesocialmedia.presentation.navigation.*NavGraph { *; }
