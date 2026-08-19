# Consumer ProGuard rules for the :feature:comment module.

# Keep comment ViewModel and constructor type metadata for Koin resolution.
# The -includedescriptorclasses flag ensures all parameter types are also kept
# so Koin DI can see them and resolve dependencies via reflection.
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.comment.presentation.**.*ViewModel* { *; }
-keep,includedescriptorclasses class com.minhtu.firesocialmedia.feature.comment.presentation.comment.CommentFeatureViewModel { *; }
-keepclassmembers class com.minhtu.firesocialmedia.feature.comment.presentation.**.*ViewModel* {
    public <init>(...);
}

# Keep StateFlow and MutableStateFlow properties used for reactive composition
-keepclassmembers class com.minhtu.firesocialmedia.feature.comment.presentation.comment.CommentFeatureViewModel {
    *** messageFlow;
    *** allComments;
    *** createCommentStatus;
    *** commentBeReplied;
    *** likedComments;
    *** likeCountList;
}

# Keep DI entry points hosting commentModule().
-keep class com.minhtu.firesocialmedia.feature.comment.di.** { *; }

# Keep comment navigation implementation and API contract resolved through DI.
-keep class com.minhtu.firesocialmedia.feature.comment.navigation.CommentNavGraphImpl { *; }
-keep interface com.minhtu.firesocialmedia.presentation.comment.CommentScreenApi { *; }
