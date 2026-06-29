# Consumer ProGuard rules for the :core library module.
# These rules are merged into the final app's R8/ProGuard config automatically
# because they are declared via consumerProguardFiles in build.gradle.kts.

# -----------------------------------------------------------------------
# Retrofit – preserve generic type signatures on ALL Retrofit API interfaces
# inside this module so R8 does NOT erase them.
# Without this, R8 collapses Response<ResponseBody> to a raw Response class
# and Retrofit's HttpServiceMethod throws:
#   ClassCastException: Class cannot be cast to ParameterizedType
# -----------------------------------------------------------------------
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep all Retrofit service interfaces in this module
-keep interface com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageApi { *; }
-keep interface com.minhtu.firesocialmedia.domain.serviceimpl.auth.AuthenticationApiService { *; }

# Keep any other Retrofit-annotated interfaces we may add later
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit itself
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }
-dontwarn retrofit2.**

# OkHttp – required for Retrofit's transport layer
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-dontwarn okhttp3.**

# Okio – OkHttp's I/O library
-keep class okio.** { *; }
-dontwarn okio.**

# Keep ResponseBody so Retrofit's built-in converter can handle it
-keep class okhttp3.ResponseBody { *; }
-keep class okhttp3.RequestBody { *; }


