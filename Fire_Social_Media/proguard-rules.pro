# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keep class com.minhtu.firesocialmedia.data.remote.dto.** { *; }
-keep class com.minhtu.firesocialmedia.data.local.entity.** { *; }
# Keep domain entities used for Firebase Database writes to prevent field-name obfuscation
-keep class com.minhtu.firesocialmedia.domain.entity.** { *; }