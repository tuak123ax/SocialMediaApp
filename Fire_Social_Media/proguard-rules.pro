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

############################################
# Koin / Kotlin reflection rules
#
# Koin uses Kotlin reflection (KClass) and inspects generic type arguments at
# runtime to resolve dependencies. Without these attributes R8 erases the
# generic information and Koin sees every type as `java.lang.Object`, which
# results in:
#   org.koin.core.error.NoDefinitionFoundException:
#       No definition found for type 'java.lang.Object'.
############################################

# Required for Koin / kotlin-reflect to read generic type information
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes KotlinMetadata
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations,RuntimeInvisibleTypeAnnotations

# Keep Koin itself – do NOT let R8 optimize or rename Koin classes,
# otherwise the reified `koinViewModel<T>()` / `get<T>()` call sites lose
# their KClass token and Koin resolves the type as java.lang.Object.
-keep class org.koin.** { *; }
-keep interface org.koin.** { *; }
-keepclassmembers class org.koin.** { *; }
-dontwarn org.koin.**
-dontnote org.koin.**

# Koin compose viewmodel reified helpers – must not be optimized,
# otherwise the `clazz: KClass<T>` argument passed to resolveViewModel
# is collapsed to Object::class by R8.
-keep,allowobfuscation class org.koin.compose.** { *; }
-keep,allowobfuscation class org.koin.viewmodel.** { *; }
-keep,allowobfuscation class org.koin.androidx.** { *; }
-keep class org.koin.core.parameter.** { *; }
-keep class org.koin.core.qualifier.** { *; }

# Keep Kotlin reflection metadata – Koin uses KClass tokens at runtime
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep interface kotlin.reflect.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-keep class kotlin.jvm.JvmClassMappingKt { *; }
-keep class kotlin.jvm.internal.Reflection { *; }
-keep class kotlin.jvm.internal.ReflectionFactory { *; }
-keep class kotlin.jvm.internal.ClassReference { *; }
-dontwarn kotlin.reflect.**

# Keep coroutines (used heavily by ViewModels)
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep all ViewModels (resolved by Koin via KClass).
# `includedescriptorclasses` ensures their constructor parameter types
# are also kept, so Koin can inject dependencies via reflection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.**.*ViewModel { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.**.*ViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.**.*ViewModel {
    public <init>(...);
}
-keepclassmembers class com.minhtu.firesocialmedia.feature.**.*ViewModel {
    public <init>(...);
}

# Keep all UseCases (resolved by Koin and injected into ViewModels)
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.**.*UseCase { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.core.domain.usecases.** { *; }

# Keep Koin module declarations (top-level Kotlin functions returning Module)
-keep class com.minhtu.firesocialmedia.**.di.** { *; }
-keep class com.minhtu.firesocialmedia.feature.**.di.** { *; }

# Keep repository and service interfaces / impls used by DI graph
-keep class com.minhtu.firesocialmedia.core.domain.repository.** { *; }
-keep class com.minhtu.firesocialmedia.**.repository.** { *; }
-keep class com.minhtu.firesocialmedia.**.serviceimpl.** { *; }
-keep interface com.minhtu.firesocialmedia.**.service.** { *; }

# Keep ViewModel contracts (interfaces bound via Koin `bind ...::class`)
-keep interface com.minhtu.firesocialmedia.**.*Contract { *; }
-keep interface com.minhtu.firesocialmedia.feature.**.*Contract { *; }

# Keep PollViewModelInterface – cast to in UiUtils.kt at runtime via `as? PollViewModelInterface`
-keep interface com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.PollViewModelInterface { *; }
-keepclassmembers interface com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.PollViewModelInterface { *; }

# Keep feature:group navigation interface and implementation
-keep interface com.minhtu.firesocialmedia.feature.group.** { *; }
-keep class com.minhtu.firesocialmedia.feature.group.navigation.GroupNavGraphImpl { *; }
-dontwarn com.minhtu.firesocialmedia.feature.group.**

# androidx.lifecycle ViewModel base class
-keep class androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# kmp-observableviewmodel
-keep class com.rickclephas.kmp.observableviewmodel.** { *; }
-dontwarn com.rickclephas.kmp.observableviewmodel.**

############################################
# Compose + Koin reified call-site protection
#
# `koinViewModel<T>()` is a reified inline function. When R8 inlines it
# into a Composable that lives in a KMP commonMain file (which compiles
# to a synthetic `*Kt` file-facade class), aggressive optimizations can
# strip the KClass<T> token and the call resolves with `Object::class`.
# To prevent that, keep:
#   - all our presentation/composable file-facade classes
#   - the entire app code in the FireSocialMedia packages
# This is broad on purpose; the app is small and correctness > size.
############################################

# Keep every class in the FireSocialMedia codebase (composables, screens,
# helpers, etc.) so reified `koinViewModel<T>()` call sites are preserved
# with their concrete type tokens intact.
-keep class com.minhtu.firesocialmedia.** { *; }
-keep interface com.minhtu.firesocialmedia.** { *; }
-keep enum com.minhtu.firesocialmedia.** { *; }

# Compose runtime / Kotlin metadata for reified inline expansion
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Lifecycle ViewModelProvider / SavedStateHandle reflection
-keep class androidx.lifecycle.viewmodel.** { *; }
-keep class androidx.lifecycle.SavedStateHandle { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

############################################
# Calling module – WebRTC (stream-webrtc-android / org.webrtc)
#
# AndroidAudioCallService, AndroidPlatformActualImpl and the
# SurfaceViewRenderer-based video views all reference WebRTC classes
# at runtime. R8 must not rename or remove any of them.
############################################
-keep class org.webrtc.** { *; }
-keep interface org.webrtc.** { *; }
-keepclassmembers class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# EglBase context is accessed via reflection inside SurfaceViewRenderer
-keep class org.webrtc.EglBase { *; }
-keep class org.webrtc.EglBase$Context { *; }

############################################
# Calling module – kotlinx.serialization
#
# CallForegroundService encodes/decodes UserDTO, OfferAnswerDTO, and
# IceCandidateDTO via Json.encodeToString / Json.decodeFromString.
# The kotlinx.serialization plugin generates companion-object serializers
# that R8 strips without these rules, causing SerializationException.
############################################
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.**

# Keep the serialization plugin's generated companion objects and descriptors
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}

# Keep serializer() methods and companion objects on all @Serializable classes
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static ** serializer(...);
    static ** $serializer;
    kotlinx.serialization.KSerializer serializer(...);
    ** Companion;
    ** INSTANCE;
}

# Keep all generated $serializer inner classes
-keep class **$$serializer { *; }

# Keep the kotlinx.serialization runtime itself
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

############################################
# Calling module – Android Service / BroadcastReceiver components
#
# CallForegroundService and CallActionBroadcastReceiver are registered
# in the manifest and started/received by the system. Their class names
# must be preserved exactly.
############################################
-keep class com.minhtu.firesocialmedia.domain.serviceimpl.call.CallForegroundService { *; }
-keep class com.minhtu.firesocialmedia.domain.serviceimpl.call.CallActionBroadcastReceiver { *; }

# Keep all Service / BroadcastReceiver subclasses in general
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }

############################################
# Calling module – CallStatus enum
#
# CallStatus is used as a @Serializable field inside AudioCallSessionDTO.
# Its enum constant names must be preserved so kotlinx.serialization can
# deserialize the string value back to the correct enum entry.
############################################
-keepclassmembers enum com.minhtu.firesocialmedia.core.domain.entity.call.CallStatus {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    *;
}

