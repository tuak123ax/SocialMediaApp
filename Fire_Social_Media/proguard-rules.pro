# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keep class com.minhtu.firesocialmedia.data.remote.dto.** { *; }
-keep class com.minhtu.firesocialmedia.data.local.entity.** { *; }

# Keep Retrofit API service interface for 2FA
-keep interface com.minhtu.firesocialmedia.domain.serviceimpl.auth.AuthenticationApiService { *; }

# Retrofit rules
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson rules – prevent stripping of fields used for JSON serialization/deserialization
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep generic type information needed by Gson
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

