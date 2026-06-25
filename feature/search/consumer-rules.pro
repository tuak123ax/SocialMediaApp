# Consumer ProGuard rules for the :feature:search module.

# Keep search ViewModel and constructor type metadata for Koin.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.search.presentation.search.*ViewModel* { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.search.presentation.search.*ViewModel* {
    public <init>(...);
}

# Keep DI module entry points hosting searchModule().
-keep class com.minhtu.firesocialmedia.feature.search.di.** { *; }

# Keep search navigation implementation and shared API contract resolved via Koin.
-keep class com.minhtu.firesocialmedia.feature.search.navigation.** { *; }
-keep interface com.minhtu.firesocialmedia.presentation.navigation.SearchNavGraph { *; }

