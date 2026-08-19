# Consumer ProGuard rules for the :feature:home module.
# These rules are merged into the app's R8 config through consumerProguardFiles.

# Keep home ViewModels and constructor type metadata for Koin injection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.home.presentation.loading.LoadingViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.home.HomeViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.home.HomeAccountViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.comment.HomeCommentFeatureViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.postinformation.PostInformationViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.share.ShareViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.home.presentation.loading.LoadingViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.home.HomeViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.home.HomeAccountViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.comment.HomeCommentFeatureViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.postinformation.PostInformationViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.share.ShareViewModel {
    public <init>(...);
}

# Keep DI module declarations hosting homeModule()/homeCommentModule().
-keep class com.minhtu.firesocialmedia.di.HomeModuleKt { *; }
-keep class com.minhtu.firesocialmedia.di.HomeCommentModuleKt { *; }
-keep class com.minhtu.firesocialmedia.di.HomeAndroidModuleKt { *; }
