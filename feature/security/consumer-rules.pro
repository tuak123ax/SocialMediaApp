# Consumer ProGuard rules for the :feature:security library module.
# These are merged into the app's R8 config through consumerProguardFiles.

# Keep security ViewModels and constructor type info for Koin injection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.security.presentation.**.*ViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.security.presentation.**.*ViewModel {
    public <init>(...);
}

# Keep top-level DI declaration hosting securityModule().
-keep class com.minhtu.firesocialmedia.feature.security.di.SecurityModuleKt { *; }

# Keep nav graph implementation resolved through Koin as SecurityNavGraph.
-keep class com.minhtu.firesocialmedia.feature.security.navigation.SecurityNavGraphImpl { *; }


