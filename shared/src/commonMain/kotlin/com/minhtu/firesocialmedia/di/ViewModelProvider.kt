package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.presentation.calling.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.presentation.calling.videocall.VideoCallViewModel
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.NotificationViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.showimage.ShowImageViewModel
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel

object ViewModelProvider {
    fun createSignInViewModel(platformContext: PlatformContext) : SignInViewModel {
        val authenticationRepository = AppModule.provideAuthenticationRepository(platformContext)
        val signInUseCase = AppModule.provideSignInUseCase(authenticationRepository)
        val rememberPasswordUseCase = AppModule.provideRememberPasswordUseCase(authenticationRepository)
        val checkUserExistsUseCase = AppModule.provideCheckUserExistsUseCase(authenticationRepository)
        val checkLocalAccountUseCase = AppModule.provideCheckLocalAccountUseCase(authenticationRepository)
        val handleSignInGoogleResult = AppModule.provideHandleSignInGoogleResultUseCase(authenticationRepository)
        return AppModule.provideSignInViewModel(
            signInUseCase,
            rememberPasswordUseCase,
            checkUserExistsUseCase,
            checkLocalAccountUseCase,
            handleSignInGoogleResult
        )
    }

    fun createSignUpViewModel(platformContext: PlatformContext): SignUpViewModel {
        val signUpRepository = AppModule.provideSignUpRepository(platformContext)
        val signUpUseCase = AppModule.provideSignUpUseCase(signUpRepository)
        return AppModule.provideSignUpViewModel(signUpUseCase)
    }

    fun createForgotPasswordViewModel(platformContext: PlatformContext) : ForgotPasswordViewModel {
        val forgotPasswordRepository = AppModule.provideForgotPasswordRepository(platformContext)
        val checkIfEmailExistsUseCase = AppModule.provideCheckIfEmailExistsUseCase(forgotPasswordRepository)
        val sendEmailResetPasswordUseCase = AppModule.provideSendEmailResetPasswordUseCase(forgotPasswordRepository)
        return AppModule.provideForgotPasswordViewModel(
            checkIfEmailExistsUseCase,
            sendEmailResetPasswordUseCase
        )
    }

    fun createInformationViewModel(platformContext: PlatformContext) : InformationViewModel {
        val informationRepository = AppModule.provideInformationRepository(platformContext)
        val userRepository = AppModule.provideUserRepository(platformContext)
        val localRepository = AppModule.provideLocalRepository(platformContext)

        val saveSignUpInformationUseCase = AppModule.provideSaveSignUpInformationUseCase(informationRepository)
        val getCurrentUserUidUseCase = AppModule.provideGetCurrentUserUidUseCase(userRepository)
        val getFCMTokenUseCase = AppModule.provideGetFCMTokenUseCase(localRepository)
        return AppModule.provideInformationViewModel(
            saveSignUpInformationUseCase,
            getCurrentUserUidUseCase,
            getFCMTokenUseCase
            )
    }

    fun createLoadingViewModel() : LoadingViewModel {
        return AppModule.provideLoadingViewModel()
    }

    fun createHomeViewModel(platformContext: PlatformContext) : HomeViewModel {
        val authenticationRepository = AppModule.provideAuthenticationRepository(platformContext)
        val notificationRepository = AppModule.provideNotificationRepository(platformContext)
        val commonDbRepository = AppModule.provideCommonDbRepository(platformContext)
        val userRepository = AppModule.provideUserRepository(platformContext)
        val newsRepository = AppModule.provideNewsRepository(platformContext)
        val callRepository = AppModule.provideCallRepository(platformContext)
        val getCurrentUserUidUseCase = AppModule.provideGetCurrentUserUidUseCase(userRepository)
        val getUserUseCase = AppModule.provideGetUserUseCase(userRepository)
        val getLatestNewsUseCase = AppModule.provideGetLatestNewsUseCase(newsRepository)
        val getAllNotificationOfUserUseCase = AppModule.provideGetAllNotificationOfUserUseCase(notificationRepository)
        val updateFCMTokenUseCase = AppModule.provideUpdateFCMTokenUseCase(userRepository)
        val clearAccountUseCase = AppModule.provideClearAccountUseCase(authenticationRepository)
        val saveValueToDatabaseUseCase = AppModule.provideSaveValueToDatabaseUseCase(commonDbRepository)
        val updateCountValueInDatabase = AppModule.provideUpdateCountValueInDatabase(commonDbRepository)
        val deleteNewsFromDatabaseUseCase = AppModule.provideDeleteNewsFromDatabaseUseCase(newsRepository)
        val sendSignalingDataUseCase = AppModule.provideSendSignalingDataUseCase(callRepository)
        val observePhoneCallWithInCallUseCase = AppModule.provideObservePhoneCallWithInCallUseCase(sendSignalingDataUseCase)
        val stopObservePhoneCallUseCase = AppModule.provideStopObservePhoneCallUseCase(sendSignalingDataUseCase)
        val saveNotificationToDatabaseUseCase = AppModule.provideSaveNotificationToDatabaseUseCase(notificationRepository)
        val deleteNotificationFromDatabaseUseCase = AppModule.provideDeleteNotificationFromDatabaseUseCase(notificationRepository)
        val searchUserByNameUseCase = AppModule.provideSearchUserByNameUseCase(userRepository)
        val stopCallServiceUseCase = AppModule.provideStopCallServiceUseCase(callRepository)
        val userInteractor = AppModule.provideUserInteractor(
            getCurrentUserUidUseCase,
            getUserUseCase,
            updateFCMTokenUseCase,
            clearAccountUseCase,
            saveValueToDatabaseUseCase,
            searchUserByNameUseCase
        )
        val newsInteractor = AppModule.provideNewsInteractor(
            getLatestNewsUseCase,
            updateCountValueInDatabase,
            deleteNewsFromDatabaseUseCase
        )
        val notificationInteractor = AppModule.provideNotificationInteractor(
            getAllNotificationOfUserUseCase,
            saveNotificationToDatabaseUseCase,
            deleteNotificationFromDatabaseUseCase
        )
        val callInteractor = AppModule.provideCallInteractor(
            observePhoneCallWithInCallUseCase,
            stopObservePhoneCallUseCase,
            stopCallServiceUseCase
        )
        return AppModule.provideHomeViewModel(
            userInteractor,
            newsInteractor,
            notificationInteractor,
            callInteractor
        )
    }

    fun createCommentViewModel(platformContext: PlatformContext) : CommentViewModel{
        val userRepository = AppModule.provideUserRepository(platformContext)
        val notificationRepository = AppModule.provideNotificationRepository(platformContext)
        val commonDbRepository = AppModule.provideCommonDbRepository(platformContext)
        val commentRepository = AppModule.provideCommentRepository(platformContext)
        val getUserUseCase = AppModule.provideGetUserUseCase(userRepository)
        val saveCommentToDatabaseUseCase = AppModule.provideSaveCommentToDatabaseUseCase(commonDbRepository)
        val saveSubCommentToDatabaseUseCase = AppModule.provideSaveSubCommentToDatabaseUseCase(commonDbRepository)
        val deleteCommentFromDatabaseUseCase = AppModule.provideDeleteCommentFromDatabaseUseCase(commonDbRepository)
        val deleteSubCommentFromDatabaseUseCase = AppModule.provideDeleteSubCommentFromDatabaseUseCase(commonDbRepository)
        val getAllCommentsUseCase = AppModule.provideGetAllCommentsUseCase(commentRepository)
        val commentInteractor = AppModule.provideCommentInteractor(
            saveCommentToDatabaseUseCase,
            saveSubCommentToDatabaseUseCase,
            deleteCommentFromDatabaseUseCase,
            deleteSubCommentFromDatabaseUseCase,
            getAllCommentsUseCase
        )
        val saveLikedCommentsUseCase = AppModule.provideSaveLikedCommentsUseCase(commonDbRepository)
        val saveNotificationToDatabaseUseCase = AppModule.provideSaveNotificationToDatabaseUseCase(notificationRepository)
        val updateCommentCountForNewUseCase = AppModule.provideUpdateCommentCountForNewUseCase(commonDbRepository)
        val updateReplyCountForCommentUseCase = AppModule.provideUpdateReplyCountForCommentUseCase(commonDbRepository)
        val updateLikeCountForCommentUseCase = AppModule.provideUpdateLikeCountForCommentUseCase(commonDbRepository)
        val updateLikeCountForSubCommentUseCase = AppModule.provideUpdateLikeCountForSubCommentUseCase(commonDbRepository)
        return AppModule.provideCommentViewModel(
            commentInteractor,
            getUserUseCase,
            saveLikedCommentsUseCase,
            saveNotificationToDatabaseUseCase,
            updateCommentCountForNewUseCase,
            updateReplyCountForCommentUseCase,
            updateLikeCountForCommentUseCase,
            updateLikeCountForSubCommentUseCase
        )
    }

    fun createUploadNewfeedViewModel(platformContext: PlatformContext) : UploadNewfeedViewModel {
        val notificationRepository = AppModule.provideNotificationRepository(platformContext)
        val userRepository = AppModule.provideUserRepository(platformContext)
        val commonDbRepository = AppModule.provideCommonDbRepository(platformContext)
        val newsRepository = AppModule.provideNewsRepository(platformContext)

        val saveNotificationToDatabaseUseCase = AppModule.provideSaveNotificationToDatabaseUseCase(notificationRepository)
        val getUserUseCase = AppModule.provideGetUserUseCase(userRepository)
        val saveNewToDatabaseUseCase = AppModule.provideSaveNewToDatabaseUseCase(commonDbRepository)
        val updateNewsFromDatabaseUseCase = AppModule.provideUpdateNewsFromDatabaseUseCase(newsRepository)
        return AppModule.provideUploadNewfeedViewModel(
            getUserUseCase,
            saveNotificationToDatabaseUseCase,
            saveNewToDatabaseUseCase,
            updateNewsFromDatabaseUseCase)
    }

    fun createUserInformationViewModel(platformContext : PlatformContext): UserInformationViewModel {
        val notificationRepository = AppModule.provideNotificationRepository(platformContext)
        val commonDbRepository = AppModule.provideCommonDbRepository(platformContext)
        val callRepository = AppModule.provideCallRepository(platformContext)
        val saveFriendUseCase = AppModule.provideSaveFriendUseCase(commonDbRepository)
        val saveFriendRequestUseCase = AppModule.provideSaveFriendRequestUseCase(commonDbRepository)
        val saveNotificationToDatabaseUseCase = AppModule.provideSaveNotificationToDatabaseUseCase(notificationRepository)
        val checkCalleeAvailableUseCase = AppModule.provideCheckCalleeAvailableUseCase(callRepository)
        return AppModule.provideUserInformationViewModel(
            saveFriendUseCase,
            saveFriendRequestUseCase,
            saveNotificationToDatabaseUseCase,
            checkCalleeAvailableUseCase)
    }

    fun createSearchViewModel() : SearchViewModel{
        return AppModule.provideSeachViewModel()
    }

    fun createFriendViewModel(platformContext : PlatformContext) : FriendViewModel {
        val commonDbRepository = AppModule.provideCommonDbRepository(platformContext)
        val saveFriendUseCase = AppModule.provideSaveFriendUseCase(commonDbRepository)
        val saveFriendRequestUseCase = AppModule.provideSaveFriendRequestUseCase(commonDbRepository)
        return AppModule.provideFriendViewModel(
            saveFriendUseCase,
            saveFriendRequestUseCase
        )
    }

    fun createShowImageViewModel(platformContext: PlatformContext) : ShowImageViewModel{
        val showImageRepository = AppModule.provideShowImageRepository(platformContext)
        val downloadImageUseCase = AppModule.provideDownloadImageUseCase(showImageRepository)
        return AppModule.provideShowImageViewModel(downloadImageUseCase)
    }

    fun createNotificationViewModel(platformContext: PlatformContext) : NotificationViewModel {
        val userRepository = AppModule.provideUserRepository(platformContext)
        val newsRepository = AppModule.provideNewsRepository(platformContext)
        val getUserUseCase = AppModule.provideGetUserUseCase(userRepository)
        val findNewByIdInDbUseCase = AppModule.provideFindNewByIdInDbUseCase(newsRepository)
        return AppModule.provideNotificationViewModel(
            getUserUseCase,
            findNewByIdInDbUseCase
        )
    }

    fun createCallingViewModel(platformContext: PlatformContext) : CallingViewModel {
        val callRepository = AppModule.provideCallRepository(platformContext)
        val startCallServiceUseCase = AppModule.provideStartCallServiceUseCase(callRepository)
        val manageCallStateUseCase = AppModule.provideManageCallStateUseCase(callRepository)
        val requestPermissionUseCase = AppModule.provideRequestPermissionUseCase(callRepository)
        return CallingViewModel(
            startCallServiceUseCase,
            manageCallStateUseCase,
            requestPermissionUseCase
        )
    }

    fun createVideoCallViewModel(platformContext: PlatformContext) : VideoCallViewModel {
        val callRepository = AppModule.provideCallRepository(platformContext)
        val startVideoCallServiceUseCase = AppModule.provideStartVideoCallServiceUseCase(callRepository)
        val requestCameraAndAudioPermissionsUseCase = AppModule.provideRequestCameraAndAudioPermissionsUseCase(callRepository)
        return VideoCallViewModel(
            startVideoCallServiceUseCase,
            requestCameraAndAudioPermissionsUseCase
        )
    }
}