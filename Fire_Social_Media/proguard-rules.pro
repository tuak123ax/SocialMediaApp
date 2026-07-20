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

# Keep Supabase Storage API interfaces and classes for profile/cover photo uploads
# Must preserve generic types for Retrofit's ServiceMethod parsing
-keep interface com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageApi { *; }
-keepclassmembers interface com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageApi {
    *** uploadFile(...);
    *** deleteFile(...);
}

-keep class com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseClient { *; }
-keep class com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorage { *; }
-keep class com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper { *; }
-keep class com.minhtu.firesocialmedia.domain.serviceimpl.database.StorageHelperInterface { *; }

# Preserve Response types from Retrofit
-keep class retrofit2.Response { *; }
-keepclassmembers class retrofit2.Response { *; }

# Preserve OkHttp RequestBody and related classes
-keep class okhttp3.RequestBody { *; }
-keep class okhttp3.RequestBody$* { *; }
-keepclassmembers class okhttp3.RequestBody { *; }

# Keep all Retrofit and OkHttp classes with all members
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }

-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }

# Retrofit rules - preserve signature attributes for generics
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep annotations on methods and parameters for Retrofit
-keepattributes *Annotation*
-keep @interface retrofit2.http.*
-keep @interface okhttp3.*
-keep @interface kotlin.Metadata

# Kotlin-specific rules - keep metadata for suspend functions and default parameters
-keep class kotlin.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-keepclassmembers class kotlin.** { *; }
-keepclassmembers class kotlin.jvm.internal.** { *; }
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes *Annotation*

# Gson rules – prevent stripping of fields used for JSON serialization/deserialization
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep generic type information needed by Gson
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

