# Consumer ProGuard rules for the :feature:auth module.
# These rules are merged into the app's R8 config through consumerProguardFiles.

# Keep auth ViewModels and constructor type metadata for Koin injection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.auth.presentation.loading.LoadingViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.information.InformationViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.presentation.signin.SignInViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.auth.presentation.loading.LoadingViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.information.InformationViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.presentation.signin.SignInViewModel {
    public <init>(...);
}

# Keep DI module declarations hosting authModule()/networkModule().
-keep class com.minhtu.firesocialmedia.di.AuthModuleKt { *; }
-keep class com.minhtu.firesocialmedia.di.NetworkModuleKt { *; }
-keep class com.minhtu.firesocialmedia.di.AuthAndroidModuleKt { *; }
