# Consumer ProGuard rules for the :feature:profile module.
# These rules are merged into the app's R8 config through consumerProguardFiles.

# Keep profile ViewModels and constructor type metadata for Koin injection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.personalinformation.PersonalInformationViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.userinformation.ProfileFriendViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.profile.EngagementViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.profile.SessionViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.profile.presentation.loading.LoadingViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.presentation.personalinformation.PersonalInformationViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.userinformation.ProfileFriendViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.profile.EngagementViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.profile.SessionViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.profile.presentation.loading.LoadingViewModel {
    public <init>(...);
}

# Keep DI module declarations hosting profileModule().
-keep class com.minhtu.firesocialmedia.di.ProfileModuleKt { *; }
-keep class com.minhtu.firesocialmedia.di.ProfileAndroidModuleKt { *; }
