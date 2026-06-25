# Consumer ProGuard rules for the :feature:friend module.

# Keep friend ViewModel and constructor type metadata for Koin.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.friend.presentation.**.*ViewModel* { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.friend.presentation.**.*ViewModel* {
    public <init>(...);
}

# Keep DI module entry points hosting friendModule().
-keep class com.minhtu.firesocialmedia.feature.friend.di.** { *; }

# Keep friend navigation implementation and shared API contract resolved via Koin.
-keep class com.minhtu.firesocialmedia.feature.friend.navigation.** { *; }
-keep interface com.minhtu.firesocialmedia.presentation.navigation.FriendNavGraph { *; }

# Keep friend contract used across modules.
-keep interface com.minhtu.firesocialmedia.presentation.friend.FriendViewModelContract { *; }
