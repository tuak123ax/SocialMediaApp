# Consumer ProGuard rules for the :feature:notification module.

# Keep notification feature ViewModels and constructor metadata for Koin.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.notification.presentation.**.*ViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.notification.presentation.**.*ViewModel {
    public <init>(...);
}

# Keep top-level DI declaration hosting notificationModule().
-keep class com.minhtu.firesocialmedia.feature.notification.di.NotificationModuleKt { *; }

# Keep nav graph implementation resolved through Koin as NotificationNavGraph.
-keep class com.minhtu.firesocialmedia.feature.notification.navigation.NotificationNavGraphImpl { *; }

