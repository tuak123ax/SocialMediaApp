# Consumer ProGuard rules for the :feature:calling module.
# These rules are merged into the app's R8 config through consumerProguardFiles.

# Keep calling ViewModels and constructor type metadata for Koin injection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.videocall.VideoCallViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.audiocall.CallViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.audiocall.CallingViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.calling.presentation.loading.LoadingViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.presentation.videocall.VideoCallViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.audiocall.CallViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.audiocall.CallingViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.calling.presentation.loading.LoadingViewModel {
    public <init>(...);
}

# Keep DI module declarations hosting callingModule().
-keep class com.minhtu.firesocialmedia.di.CallingModuleKt { *; }
-keep class com.minhtu.firesocialmedia.di.CallingAndroidModuleKt { *; }

# WebRTC classes referenced at runtime by AndroidAudioCallService / video views.
-keep class org.webrtc.** { *; }
-keep interface org.webrtc.** { *; }
-keepclassmembers class org.webrtc.** { *; }
-dontwarn org.webrtc.**
