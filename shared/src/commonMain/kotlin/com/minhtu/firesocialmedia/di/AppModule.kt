package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.application.interactor.CallInteractorImpl
import com.minhtu.firesocialmedia.application.interactor.CommentInteractorImpl
import com.minhtu.firesocialmedia.application.interactor.NewsInteractorImpl
import com.minhtu.firesocialmedia.application.interactor.NotificationInteractorImpl
import com.minhtu.firesocialmedia.application.interactor.UserInteractorImpl
import com.minhtu.firesocialmedia.data.repository.AuthenticationRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.CallRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.CommentRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.CommonDbRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.GroupRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.LocalRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.NetworkRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.NewsRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.NotificationRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.SettingsRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.ShowImageRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.UserRepositoryImpl
import com.minhtu.firesocialmedia.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.CallInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.domain.repository.CommentRepository
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.domain.repository.NetworkRepository
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.domain.repository.ShowImageRepository
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObservePhoneCallWithInCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendSignalingDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StopCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StopObservePhoneCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateCameraStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateMicStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateSpeakerStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.DeleteCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.DeleteSubCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.GetAllCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveSubCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.CheckIfEmailExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.SendEmailResetPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchRecommendGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllMembersInGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetGroupConfigsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.InviteFriendToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.SaveNewToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.UpdateNotificationStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearLocalDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearLocalFriendsUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.DeleteNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetLatestNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SaveCurrentUserInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreNewsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreNotificationsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreUserFriendsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.domain.usecases.network.CheckInternetConnectionUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.DeleteAllDraftPostsUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.DeleteDraftPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SaveNewToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.UpdateNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteAllNotificationsUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.GetAllNotificationOfUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.UpdateIsReadStatusOfNotificationUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.BuildOtpAuthUrlUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.ChangePasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Disable2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Enable2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.GenerateSecretFor2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Get2FAVerifiedStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserAvatarUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserBackgroundUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserStringFieldUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserTimestampUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateVerify2FASuccessUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.ValidateNewPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Verify2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyBackupCodeUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.showimage.DownloadImageUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.domain.usecases.signup.SignUpUseCase
import com.minhtu.firesocialmedia.domain.usecases.sync.LoadNewsPostedWhenOfflineUseCase
import com.minhtu.firesocialmedia.domain.usecases.sync.SyncDataUseCase
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.NotificationViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.personal.PersonalInformationViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.showimage.ShowImageViewModel
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel

object AppModule {
    fun provideSaveNotificationToDatabaseUseCase(notificationRepository: NotificationRepository): SaveNotificationToDatabaseUseCase {
        return SaveNotificationToDatabaseUseCase(notificationRepository)
    }

    fun provideDeleteNotificationFromDatabaseUseCase(notificationRepository: NotificationRepository): DeleteNotificationFromDatabaseUseCase {
        return DeleteNotificationFromDatabaseUseCase(notificationRepository)
    }

    fun provideSaveValueToDatabaseUseCase(commonDbRepository: CommonDbRepository): SaveLikedPostUseCase {
        return SaveLikedPostUseCase(commonDbRepository)
    }

    fun provideSaveLikedCommentsUseCase(commonDbRepository: CommonDbRepository): SaveLikedCommentsUseCase {
        return SaveLikedCommentsUseCase(commonDbRepository)
    }

    fun provideNotificationRepository(platformContext: PlatformContext): NotificationRepository {
        return NotificationRepositoryImpl(
            platformContext.database,
            platformContext.room,
            platformContext.networkMonitor
        )
    }

    fun provideCommonDbRepository(platformContext: PlatformContext): CommonDbRepository {
        return CommonDbRepositoryImpl(
            platformContext.database,
            platformContext.room,
            platformContext.networkMonitor
        )
    }

    //---------------------------Sign in----------------------------------------//
    fun provideSignInUseCase(authenticationRepository: AuthenticationRepository): SignInUseCase {
        return SignInUseCase(authenticationRepository)
    }

    fun provideRememberPasswordUseCase(authenticationRepository: AuthenticationRepository): RememberPasswordUseCase {
        return RememberPasswordUseCase(authenticationRepository)
    }

    fun provideCheckUserExistsUseCase(authenticationRepository: AuthenticationRepository): CheckUserExistsUseCase {
        return CheckUserExistsUseCase(authenticationRepository)
    }

    fun provideCheckLocalAccountUseCase(authenticationRepository: AuthenticationRepository): CheckLocalAccountUseCase {
        return CheckLocalAccountUseCase(authenticationRepository)
    }

    fun provideHandleSignInGoogleResultUseCase(authenticationRepository: AuthenticationRepository): HandleSignInGoogleResultUseCase {
        return HandleSignInGoogleResultUseCase(authenticationRepository)
    }

    fun provideSignInViewModel(
        signInUseCase: SignInUseCase,
        rememberPasswordUseCase: RememberPasswordUseCase,
        checkUserExistsUseCase: CheckUserExistsUseCase,
        checkLocalAccountUseCase: CheckLocalAccountUseCase,
        handleSignInGoogleResult: HandleSignInGoogleResultUseCase,
        getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
        getUserUseCase: GetUserUseCase,
        saveLoginActivityInfoUseCase: SaveLoginActivityInfoUseCase
    ): SignInViewModel {
        return SignInViewModel(
            signInUseCase,
            rememberPasswordUseCase,
            checkUserExistsUseCase,
            checkLocalAccountUseCase,
            handleSignInGoogleResult,
            getCurrentUserUidUseCase,
            getUserUseCase,
            saveLoginActivityInfoUseCase
        )
    }

    fun provideAuthenticationRepository(platformContext: PlatformContext): AuthenticationRepository {
        return AuthenticationRepositoryImpl(
            platformContext.auth,
            platformContext.database,
            platformContext.crypto
        )
    }

    //---------------------------Sign up----------------------------------------//
    fun provideSignUpRepository(platformContext: PlatformContext): AuthenticationRepository {
        return AuthenticationRepositoryImpl(
            platformContext.auth,
            platformContext.database,
            platformContext.crypto
        )
    }

    fun provideSignUpUseCase(authenticationRepository: AuthenticationRepository): SignUpUseCase {
        return SignUpUseCase(authenticationRepository)
    }

    fun provideSignUpViewModel(signUpUseCase: SignUpUseCase): SignUpViewModel {
        return SignUpViewModel(signUpUseCase)
    }

    //---------------------------Forgot password----------------------------------------//
    fun provideForgotPasswordRepository(platformContext: PlatformContext): AuthenticationRepository {
        return AuthenticationRepositoryImpl(
            platformContext.auth,
            platformContext.database,
            platformContext.crypto
        )
    }

    fun provideCheckIfEmailExistsUseCase(authenticationRepository: AuthenticationRepository): CheckIfEmailExistsUseCase {
        return CheckIfEmailExistsUseCase(authenticationRepository)
    }

    fun provideSendEmailResetPasswordUseCase(authenticationRepository: AuthenticationRepository): SendEmailResetPasswordUseCase {
        return SendEmailResetPasswordUseCase(authenticationRepository)
    }

    fun provideForgotPasswordViewModel(
        checkIfEmailExistsUseCase: CheckIfEmailExistsUseCase,
        sendEmailResetPasswordUseCase: SendEmailResetPasswordUseCase
    ): ForgotPasswordViewModel {
        return ForgotPasswordViewModel(
            checkIfEmailExistsUseCase,
            sendEmailResetPasswordUseCase
        )
    }

    //---------------------------Information----------------------------------------//
    fun provideInformationRepository(platformContext: PlatformContext): AuthenticationRepository {
        return AuthenticationRepositoryImpl(
            platformContext.auth,
            platformContext.database,
            platformContext.crypto
        )
    }

    fun provideLocalRepository(platformContext: PlatformContext): LocalRepository {
        return LocalRepositoryImpl(platformContext.crypto, platformContext.room)
    }

    fun provideSaveSignUpInformationUseCase(authenticationRepository: AuthenticationRepository): SaveSignUpInformationUseCase {
        return SaveSignUpInformationUseCase(authenticationRepository)
    }

    fun provideGetFCMTokenUseCase(localRepository: LocalRepository): GetFCMTokenUseCase {
        return GetFCMTokenUseCase(localRepository)
    }

    fun provideInformationViewModel(
        saveSignUpInformationUseCase: SaveSignUpInformationUseCase,
        getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
        getFCMTokenUseCase: GetFCMTokenUseCase,
        saveLoginActivityInfoUseCase: SaveLoginActivityInfoUseCase
    ): InformationViewModel {
        return InformationViewModel(
            saveSignUpInformationUseCase,
            getCurrentUserUidUseCase,
            getFCMTokenUseCase,
            saveLoginActivityInfoUseCase
        )
    }

    //---------------------------Loading----------------------------------------//
    fun provideLoadingViewModel(): LoadingViewModel {
        return LoadingViewModel()
    }

    //---------------------------Home----------------------------------------//
    fun provideGetCurrentUserUidUseCase(userRepository: UserRepository): GetCurrentUserUidUseCase {
        return GetCurrentUserUidUseCase(userRepository)
    }

    fun provideGetLatestNewsUseCase(newsRepository: NewsRepository): GetLatestNewsUseCase {
        return GetLatestNewsUseCase(newsRepository)
    }

    fun provideGetAllNotificationOfUserUseCase(notificationRepository: NotificationRepository): GetAllNotificationOfUserUseCase {
        return GetAllNotificationOfUserUseCase(notificationRepository)
    }

    fun provideUpdateFCMTokenUseCase(userRepository: UserRepository): UpdateFCMTokenUseCase {
        return UpdateFCMTokenUseCase(userRepository)
    }

    fun provideClearAccountUseCase(authenticationRepository: AuthenticationRepository): ClearAccountUseCase {
        return ClearAccountUseCase(authenticationRepository)
    }

    fun provideUpdateCountValueInDatabase(commonDbRepository: CommonDbRepository): UpdateLikeCountForNewUseCase {
        return UpdateLikeCountForNewUseCase(commonDbRepository)
    }

    fun provideDeleteNewsFromDatabaseUseCase(newsRepository: NewsRepository): DeleteNewsFromDatabaseUseCase {
        return DeleteNewsFromDatabaseUseCase(newsRepository)
    }

    fun provideObservePhoneCallWithInCallUseCase(sendSignalingDataUseCase: SendSignalingDataUseCase): ObservePhoneCallWithInCallUseCase {
        return ObservePhoneCallWithInCallUseCase(sendSignalingDataUseCase)
    }

    fun provideStopObservePhoneCallUseCase(sendSignalingDataUseCase: SendSignalingDataUseCase): StopObservePhoneCallUseCase {
        return StopObservePhoneCallUseCase(sendSignalingDataUseCase)
    }

    fun provideStopCallServiceUseCase(callRepository: CallRepository): StopCallServiceUseCase {
        return StopCallServiceUseCase(callRepository)
    }

    fun provideSearchUserByNameUseCase(userRepository: UserRepository): SearchUserByNameUseCase {
        return SearchUserByNameUseCase(userRepository)
    }

    fun provideSendSignalingDataUseCase(callRepository: CallRepository): SendSignalingDataUseCase {
        return SendSignalingDataUseCase(callRepository)
    }

    fun provideUserInteractor(
        getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
        getUserUseCase: GetUserUseCase,
        updateFCMTokenUseCase: UpdateFCMTokenUseCase,
        clearAccountUseCase: ClearAccountUseCase,
        saveValueToDatabaseUseCase: SaveLikedPostUseCase,
        searchUserByNameUseCase: SearchUserByNameUseCase,
        storeUserFriendsToRoomUseCase: StoreUserFriendsToRoomUseCase,
        saveCurrentUserInfoUseCase: SaveCurrentUserInfoUseCase,
        clearLocalDataUseCase: ClearLocalDataUseCase,
        clearLocalFriendsUseCase: ClearLocalFriendsUseCase
    ): UserInteractor {
        return UserInteractorImpl(
            getCurrentUserUidUseCase,
            getUserUseCase,
            updateFCMTokenUseCase,
            clearAccountUseCase,
            saveValueToDatabaseUseCase,
            searchUserByNameUseCase,
            storeUserFriendsToRoomUseCase,
            saveCurrentUserInfoUseCase,
            clearLocalDataUseCase,
            clearLocalFriendsUseCase
        )
    }

    fun provideNewsInteractor(
        getLatestNewsUseCase: GetLatestNewsUseCase,
        updateCountValueInDatabase: UpdateLikeCountForNewUseCase,
        deleteNewsFromDatabaseUseCase: DeleteNewsFromDatabaseUseCase,
        storeNewsToRoomUseCase: StoreNewsToRoomUseCase,
        saveNewToDatabaseUseCase: SaveNewToDatabaseUseCase,
        findNewByIdInDbUseCase: FindNewByIdInDbUseCase
    ): NewsInteractor {
        return NewsInteractorImpl(
            getLatestNewsUseCase,
            updateCountValueInDatabase,
            deleteNewsFromDatabaseUseCase,
            storeNewsToRoomUseCase,
            saveNewToDatabaseUseCase,
            findNewByIdInDbUseCase
        )
    }

    fun provideNotificationInteractor(
        getAllNotificationOfUserUseCase: GetAllNotificationOfUserUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        deleteNotificationFromDatabaseUseCase: DeleteNotificationFromDatabaseUseCase,
        storeNotificationsToRoomUseCase: StoreNotificationsToRoomUseCase
    ): NotificationInteractor {
        return NotificationInteractorImpl(
            getAllNotificationOfUserUseCase,
            saveNotificationToDatabaseUseCase,
            deleteNotificationFromDatabaseUseCase,
            storeNotificationsToRoomUseCase
        )
    }

    fun provideCallInteractor(
        observePhoneCallWithInCallUseCase: ObservePhoneCallWithInCallUseCase,
        stopObservePhoneCallUseCase: StopObservePhoneCallUseCase,
        stopCallServiceUseCase: StopCallServiceUseCase
    ): CallInteractor {
        return CallInteractorImpl(
            observePhoneCallWithInCallUseCase,
            stopObservePhoneCallUseCase,
            stopCallServiceUseCase
        )
    }

    fun provideHomeViewModel(
        userInteractor: UserInteractor,
        newsInteractor: NewsInteractor,
        notificationInteractor: NotificationInteractor,
        callInteractor: CallInteractor
    ): HomeViewModel {
        return HomeViewModel(
            userInteractor,
            newsInteractor,
            notificationInteractor,
            callInteractor
        )
    }

    //---------------------------Comment----------------------------------------//
    fun provideCommentRepository(platformContext: PlatformContext): CommentRepository {
        return CommentRepositoryImpl(platformContext.database)
    }

    fun provideSaveCommentToDatabaseUseCase(commonDbRepository: CommonDbRepository): SaveCommentToDatabaseUseCase {
        return SaveCommentToDatabaseUseCase(commonDbRepository)
    }

    fun provideSaveSubCommentToDatabaseUseCase(commonDbRepository: CommonDbRepository): SaveSubCommentToDatabaseUseCase {
        return SaveSubCommentToDatabaseUseCase(commonDbRepository)
    }

    fun provideDeleteCommentFromDatabaseUseCase(commonDbRepository: CommonDbRepository): DeleteCommentFromDatabaseUseCase {
        return DeleteCommentFromDatabaseUseCase(commonDbRepository)
    }

    fun provideDeleteSubCommentFromDatabaseUseCase(commonDbRepository: CommonDbRepository): DeleteSubCommentFromDatabaseUseCase {
        return DeleteSubCommentFromDatabaseUseCase(commonDbRepository)
    }

    fun provideGetAllCommentsUseCase(commentRepository: CommentRepository): GetAllCommentsUseCase {
        return GetAllCommentsUseCase(commentRepository)
    }

    fun provideUpdateCommentCountForNewUseCase(commonDbRepository: CommonDbRepository): UpdateCommentCountForNewUseCase {
        return UpdateCommentCountForNewUseCase(commonDbRepository)
    }

    fun provideUpdateReplyCountForCommentUseCase(commonDbRepository: CommonDbRepository): UpdateReplyCountForCommentUseCase {
        return UpdateReplyCountForCommentUseCase(commonDbRepository)
    }

    fun provideCommentInteractor(
        saveCommentToDatabaseUseCase: SaveCommentToDatabaseUseCase,
        saveSubCommentToDatabaseUseCase: SaveSubCommentToDatabaseUseCase,
        deleteCommentFromDatabaseUseCase: DeleteCommentFromDatabaseUseCase,
        deleteSubCommentFromDatabaseUseCase: DeleteSubCommentFromDatabaseUseCase,
        getAllCommentsUseCase: GetAllCommentsUseCase
    ): CommentInteractor {
        return CommentInteractorImpl(
            saveCommentToDatabaseUseCase,
            saveSubCommentToDatabaseUseCase,
            deleteCommentFromDatabaseUseCase,
            deleteSubCommentFromDatabaseUseCase,
            getAllCommentsUseCase
        )
    }

    fun provideUpdateLikeCountForCommentUseCase(commonDbRepository: CommonDbRepository): UpdateLikeCountForCommentUseCase {
        return UpdateLikeCountForCommentUseCase(commonDbRepository)
    }

    fun provideUpdateLikeCountForSubCommentUseCase(commonDbRepository: CommonDbRepository): UpdateLikeCountForSubCommentUseCase {
        return UpdateLikeCountForSubCommentUseCase(commonDbRepository)
    }

    fun provideCommentViewModel(
        commentInteractor: CommentInteractor,
        getUserUseCase: GetUserUseCase,
        saveLikedCommentsUseCase: SaveLikedCommentsUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        updateCommentCountForNewUseCase: UpdateCommentCountForNewUseCase,
        updateReplyCountForCommentUseCase: UpdateReplyCountForCommentUseCase,
        updateLikeCountForCommentUseCase: UpdateLikeCountForCommentUseCase,
        updateLikeCountForSubCommentUseCase: UpdateLikeCountForSubCommentUseCase
    ): CommentViewModel {
        return CommentViewModel(
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

    //---------------------------Upload newfeed----------------------------------------//
    fun provideSaveNewToDatabaseUseCase(commonDbRepository: CommonDbRepository): SaveNewToDatabaseUseCase {
        return SaveNewToDatabaseUseCase(commonDbRepository)
    }

    fun provideUpdateNewsFromDatabaseUseCase(newsRepository: NewsRepository): UpdateNewsFromDatabaseUseCase {
        return UpdateNewsFromDatabaseUseCase(newsRepository)
    }

    fun provideUploadNewfeedViewModel(
        getUserUseCase: GetUserUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        saveNewToDatabaseUseCase: SaveNewToDatabaseUseCase,
        updateNewsFromDatabaseUseCase: UpdateNewsFromDatabaseUseCase,
        loadNewsPostedWhenOfflineUseCase: LoadNewsPostedWhenOfflineUseCase,
        deleteAllDraftPostsUseCase: DeleteAllDraftPostsUseCase,
        deleteDraftPostUseCase: DeleteDraftPostUseCase,
        saveNewToGroupUseCase: SaveNewToGroupUseCase,
        getAllMembersInGroupUseCase: GetAllMembersInGroupUseCase,
        getGroupConfigsUseCase: GetGroupConfigsUseCase
    ): UploadNewfeedViewModel {
        return UploadNewfeedViewModel(
            getUserUseCase,
            saveNotificationToDatabaseUseCase,
            saveNewToDatabaseUseCase,
            updateNewsFromDatabaseUseCase,
            loadNewsPostedWhenOfflineUseCase,
            deleteAllDraftPostsUseCase,
            deleteDraftPostUseCase,
            saveNewToGroupUseCase,
            getAllMembersInGroupUseCase,
            getGroupConfigsUseCase
        )
    }

    //---------------------------User Information----------------------------------------//
    fun provideCallRepository(platformContext: PlatformContext): CallRepository {
        return CallRepositoryImpl(
            { platformContext.audioCall },
            platformContext.database,
            platformContext.permissionManager
        )
    }

    fun provideCheckCalleeAvailableUseCase(callRepository: CallRepository): CheckCalleeAvailableUseCase {
        return CheckCalleeAvailableUseCase(callRepository)
    }

    //---------------------------Friend----------------------------------------//
    fun provideSaveFriendUseCase(commonDbRepository: CommonDbRepository): SaveFriendUseCase {
        return SaveFriendUseCase(commonDbRepository)
    }

    fun provideSaveFriendRequestUseCase(commonDbRepository: CommonDbRepository): SaveFriendRequestUseCase {
        return SaveFriendRequestUseCase(commonDbRepository)
    }

    fun provideFriendViewModel(
        saveFriendUseCase: SaveFriendUseCase,
        saveFriendRequestUseCase: SaveFriendRequestUseCase
    ): FriendViewModel {
        return FriendViewModel(
            saveFriendUseCase,
            saveFriendRequestUseCase
        )
    }

    //---------------------------Search----------------------------------------//
    fun provideSeachViewModel(): SearchViewModel {
        return SearchViewModel()
    }

    //---------------------------Show Image----------------------------------------//
    fun provideShowImageRepository(platformContext: PlatformContext): ShowImageRepository {
        return ShowImageRepositoryImpl(platformContext.database)
    }

    fun provideDownloadImageUseCase(showImageRepository: ShowImageRepository): DownloadImageUseCase {
        return DownloadImageUseCase(showImageRepository)
    }

    fun provideShowImageViewModel(downloadImageUseCase: DownloadImageUseCase): ShowImageViewModel {
        return ShowImageViewModel(downloadImageUseCase)
    }

    //---------------------------Notification----------------------------------------//
    fun provideUserRepository(platformContext: PlatformContext): UserRepository {
        return UserRepositoryImpl(
            platformContext.auth,
            platformContext.database,
            platformContext.crypto,
            platformContext.room,
            platformContext.networkMonitor
        )
    }

    fun provideNewsRepository(platformContext: PlatformContext): NewsRepository {
        return NewsRepositoryImpl(
            platformContext.database,
            platformContext.room,
            platformContext.networkMonitor
        )
    }

    fun provideGetUserUseCase(userRepository: UserRepository): GetUserUseCase {
        return GetUserUseCase(userRepository)
    }

    fun provideFindNewByIdInDbUseCase(newsRepository: NewsRepository): FindNewByIdInDbUseCase {
        return FindNewByIdInDbUseCase(newsRepository)
    }

    fun provideNotificationViewModel(
        getUserUseCase: GetUserUseCase,
        findNewByIdInDbUseCase: FindNewByIdInDbUseCase,
        updateIsReadStatusOfNotificationUseCase: UpdateIsReadStatusOfNotificationUseCase,
        deleteAllNotificationsUseCase: DeleteAllNotificationsUseCase
    ): NotificationViewModel {
        return NotificationViewModel(
            getUserUseCase,
            findNewByIdInDbUseCase,
            updateIsReadStatusOfNotificationUseCase,
            deleteAllNotificationsUseCase
        )
    }

    //---------------------------Call----------------------------------------//
    fun provideStartCallServiceUseCase(callRepository: CallRepository): StartCallServiceUseCase {
        return StartCallServiceUseCase(callRepository)
    }

    fun provideManageCallStateUseCase(callRepository: CallRepository): ManageCallStateUseCase {
        return ManageCallStateUseCase(callRepository)
    }

    fun provideRequestPermissionUseCase(callRepository: CallRepository): RequestPermissionUseCase {
        return RequestPermissionUseCase(callRepository)
    }

    fun provideStartVideoCallServiceUseCase(callRepository: CallRepository): StartVideoCallServiceUseCase {
        return StartVideoCallServiceUseCase(callRepository)
    }

    fun provideRequestCameraAndAudioPermissionsUseCase(callRepository: CallRepository): RequestCameraAndAudioPermissionsUseCase {
        return RequestCameraAndAudioPermissionsUseCase(callRepository)
    }

    fun provideInitializeCallUseCase(callRepository: CallRepository): InitializeCallUseCase {
        return InitializeCallUseCase(callRepository)
    }

    fun provideVideoCallUseCase(callRepository: CallRepository): VideoCallUseCase {
        return VideoCallUseCase(callRepository)
    }

    //---------------------------Room----------------------------------------//
    fun provideStoreNewsToRoomUseCase(localRepository: LocalRepository): StoreNewsToRoomUseCase {
        return StoreNewsToRoomUseCase(localRepository)
    }

    fun provideStoreNotificationsToRoomUseCase(localRepository: LocalRepository): StoreNotificationsToRoomUseCase {
        return StoreNotificationsToRoomUseCase(localRepository)
    }

    fun provideStoreUserFriendsToRoomUseCase(localRepository: LocalRepository): StoreUserFriendsToRoomUseCase {
        return StoreUserFriendsToRoomUseCase(localRepository)
    }

    fun provideSaveCurrentUserInfoUseCase(localRepository: LocalRepository): SaveCurrentUserInfoUseCase {
        return SaveCurrentUserInfoUseCase(localRepository)
    }

    fun provideSyncDataUseCase(commonDbRepository: CommonDbRepository): SyncDataUseCase {
        return SyncDataUseCase(commonDbRepository)
    }

    fun provideClearLocalDataUseCase(commonDbRepository: CommonDbRepository): ClearLocalDataUseCase {
        return ClearLocalDataUseCase(commonDbRepository)
    }

    fun provideLoadNewsPostedWhenOfflineUseCase(commonDbRepository: CommonDbRepository): LoadNewsPostedWhenOfflineUseCase {
        return LoadNewsPostedWhenOfflineUseCase(commonDbRepository)
    }

    fun provideDeleteAllDraftPostsUseCase(commonDbRepository: CommonDbRepository): DeleteAllDraftPostsUseCase {
        return DeleteAllDraftPostsUseCase(commonDbRepository)
    }

    fun provideDeleteDraftPostUseCase(commonDbRepository: CommonDbRepository): DeleteDraftPostUseCase {
        return DeleteDraftPostUseCase(commonDbRepository)
    }

    //---------------------------Group----------------------------------------//
    fun provideCreateGroupUseCase(groupRepository: GroupRepository): CreateGroupUseCase {
        return CreateGroupUseCase(groupRepository)
    }

    fun provideGroupRepository(platformContext: PlatformContext): GroupRepository {
        return GroupRepositoryImpl(
            platformContext.database,
            platformContext.networkMonitor,
            platformContext.clipboard
        )
    }

    fun provideGetAllGroupsUseCase(groupRepository: GroupRepository): GetAllGroupsUseCase {
        return GetAllGroupsUseCase(groupRepository)
    }

    fun provideFetchGroupInfoUseCase(groupRepository: GroupRepository): FetchGroupInfoUseCase {
        return FetchGroupInfoUseCase(groupRepository)
    }

    fun provideSaveNewToGroupUseCase(groupRepository: GroupRepository): SaveNewToGroupUseCase {
        return SaveNewToGroupUseCase(groupRepository)
    }

    fun provideUpdateNotificationStatusUseCase(groupRepository: GroupRepository): UpdateNotificationStatusUseCase {
        return UpdateNotificationStatusUseCase(groupRepository)
    }

    fun provideGetAllMembersInGroupUseCase(groupRepository: GroupRepository): GetAllMembersInGroupUseCase {
        return GetAllMembersInGroupUseCase(groupRepository)
    }

    fun provideGetGroupConfigsUseCase(groupRepository: GroupRepository): GetGroupConfigsUseCase {
        return GetGroupConfigsUseCase(groupRepository)
    }

    fun provideFetchNotificationStateUseCase(groupRepository: GroupRepository): FetchNotificationStateUseCase {
        return FetchNotificationStateUseCase(groupRepository)
    }

    fun provideCopyLinkUseCase(groupRepository: GroupRepository): CopyLinkUseCase {
        return CopyLinkUseCase(groupRepository)
    }

    fun provideFindGroupByIdUseCase(groupRepository: GroupRepository): FindGroupByIdUseCase {
        return FindGroupByIdUseCase(groupRepository)
    }

    fun provideInviteFriendToGroupUseCase(groupRepository: GroupRepository): InviteFriendToGroupUseCase {
        return InviteFriendToGroupUseCase(groupRepository)
    }

    fun provideClearLocalFriendsUseCase(commonDbRepository: CommonDbRepository): ClearLocalFriendsUseCase {
        return ClearLocalFriendsUseCase(commonDbRepository)
    }

    fun provideJoinGroupUseCase(groupRepository: GroupRepository): JoinGroupUseCase {
        return JoinGroupUseCase(groupRepository)
    }

    fun provideLeaveGroupUseCase(groupRepository: GroupRepository): LeaveGroupUseCase {
        return LeaveGroupUseCase(groupRepository)
    }

    fun provideLeaveAndDeleteGroupUseCase(groupRepository: GroupRepository): LeaveAndDeleteGroupUseCase {
        return LeaveAndDeleteGroupUseCase(groupRepository)
    }

    fun provideRemoveMemberUseCase(groupRepository: GroupRepository): RemoveMemberUseCase {
        return RemoveMemberUseCase(groupRepository)
    }

    fun providePromoteMemberUseCase(groupRepository: GroupRepository): PromoteMemberUseCase {
        return PromoteMemberUseCase(groupRepository)
    }

    fun provideDemoteMemberUseCase(groupRepository: GroupRepository): DemoteMemberUseCase {
        return DemoteMemberUseCase(groupRepository)
    }

    fun provideFetchRecommendGroupsUseCase(groupRepository: GroupRepository): FetchRecommendGroupsUseCase {
        return FetchRecommendGroupsUseCase(groupRepository)
    }

    fun provideFetchFeatureGroupsUseCase(groupRepository: GroupRepository): FetchFeatureGroupsUseCase {
        return FetchFeatureGroupsUseCase(groupRepository)
    }

    fun provideCheckInternetConnectionUseCase(networkRepository: NetworkRepository): CheckInternetConnectionUseCase {
        return CheckInternetConnectionUseCase(networkRepository)
    }

    fun provideNetworkRepository(platformContext: PlatformContext): NetworkRepository {
        return NetworkRepositoryImpl(platformContext.networkMonitor)
    }

    fun provideUpdateIsReadStatusOfNotificationUseCase(notificationRepository: NotificationRepository): UpdateIsReadStatusOfNotificationUseCase {
        return UpdateIsReadStatusOfNotificationUseCase(notificationRepository)
    }

    fun provideDeleteAllNotificationsUseCase(notificationRepository: NotificationRepository): DeleteAllNotificationsUseCase {
        return DeleteAllNotificationsUseCase(notificationRepository)
    }

    fun provideUpdateMicStatusUseCase(callRepository: CallRepository): UpdateMicStatusUseCase {
        return UpdateMicStatusUseCase(callRepository)
    }

    fun provideUpdateCameraStatusUseCase(callRepository: CallRepository): UpdateCameraStatusUseCase {
        return UpdateCameraStatusUseCase(callRepository)
    }

    fun provideUpdateSpeakerStatusUseCase(callRepository: CallRepository): UpdateSpeakerStatusUseCase {
        return UpdateSpeakerStatusUseCase(callRepository)
    }

    fun provideSettingsRepository(platformContext: PlatformContext): SettingsRepository {
        return SettingsRepositoryImpl(
            platformContext.auth,
            platformContext.database,
            platformContext.clipboard,
            platformContext.crypto
        )
    }

    fun provideVerifyCurrentPasswordUseCase(settingsRepository: SettingsRepository): VerifyCurrentPasswordUseCase {
        return VerifyCurrentPasswordUseCase(settingsRepository)
    }

    fun provideValidateNewPasswordUseCase(): ValidateNewPasswordUseCase {
        return ValidateNewPasswordUseCase()
    }

    fun provideChangePasswordUseCase(settingsRepository: SettingsRepository): ChangePasswordUseCase {
        return ChangePasswordUseCase(settingsRepository)
    }

    fun provideGenerateSecretFor2FAUseCase(settingsRepository: SettingsRepository): GenerateSecretFor2FAUseCase {
        return GenerateSecretFor2FAUseCase(settingsRepository)
    }

    fun provideBuildOtpAuthUrlUseCase(): BuildOtpAuthUrlUseCase {
        return BuildOtpAuthUrlUseCase()
    }

    fun provideCopyUseCase(settingsRepository: SettingsRepository): CopyUseCase {
        return CopyUseCase(settingsRepository)
    }

    fun provideEnable2FAUseCase(settingsRepository: SettingsRepository): Enable2FAUseCase {
        return Enable2FAUseCase(settingsRepository)
    }

    fun provideDisable2FAUseCase(settingsRepository: SettingsRepository): Disable2FAUseCase {
        return Disable2FAUseCase(settingsRepository)

    }

    fun provideVerify2FAUseCase(settingsRepository: SettingsRepository): Verify2FAUseCase {
        return Verify2FAUseCase(settingsRepository)
    }

    fun provideVerifyBackupCodeUseCase(settingsRepository: SettingsRepository): VerifyBackupCodeUseCase {
        return VerifyBackupCodeUseCase(settingsRepository)
    }

    fun provideUpdateVerify2FASuccessUseCase(settingsRepository: SettingsRepository): UpdateVerify2FASuccessUseCase {
        return UpdateVerify2FASuccessUseCase(settingsRepository)
    }

    fun provideGet2FAVerifiedStatusUseCase(settingsRepository: SettingsRepository): Get2FAVerifiedStatusUseCase {
        return Get2FAVerifiedStatusUseCase(settingsRepository)
    }

    fun provideFetchLoginHistoryListUseCase(settingsRepository: SettingsRepository): FetchLoginHistoryListUseCase {
        return FetchLoginHistoryListUseCase(settingsRepository)
    }

    fun provideUpdateUserTimestampUseCase(settingsRepository: SettingsRepository): UpdateUserTimestampUseCase {
        return UpdateUserTimestampUseCase(settingsRepository)
    }

    fun provideSaveLoginActivityInfoUseCase(commonDbRepository: CommonDbRepository): SaveLoginActivityInfoUseCase {
        return SaveLoginActivityInfoUseCase(commonDbRepository)
    }

    fun provideUpdateUserAvatarUseCase(settingsRepository: SettingsRepository): UpdateUserAvatarUseCase {
        return UpdateUserAvatarUseCase(settingsRepository)
    }

    fun provideUpdateUserBackgroundUseCase(settingsRepository: SettingsRepository): UpdateUserBackgroundUseCase {
        return UpdateUserBackgroundUseCase(settingsRepository)
    }

    fun providePersonalInformationViewModel(
        updateUserStringFieldUseCase: UpdateUserStringFieldUseCase,
        updateUserAvatarUseCase: UpdateUserAvatarUseCase,
        verifyCurrentPasswordUseCase: VerifyCurrentPasswordUseCase,
        getUserUseCase: GetUserUseCase
    ): PersonalInformationViewModel {
        return PersonalInformationViewModel(
            updateUserStringFieldUseCase,
            updateUserAvatarUseCase,
            verifyCurrentPasswordUseCase,
            getUserUseCase
        )
    }

    fun provideUserInformationViewModel(
        saveFriendUseCase: SaveFriendUseCase,
        saveFriendRequestUseCase: SaveFriendRequestUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        checkCalleeAvailableUseCase: CheckCalleeAvailableUseCase,
        getUserUseCase: GetUserUseCase,
        checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
        updateUserBackgroundUseCase: UpdateUserBackgroundUseCase
    ): UserInformationViewModel {
        return UserInformationViewModel(
            saveFriendUseCase,
            saveFriendRequestUseCase,
            saveNotificationToDatabaseUseCase,
            checkCalleeAvailableUseCase,
            getUserUseCase,
            checkInternetConnectionUseCase,
            updateUserBackgroundUseCase
        )
    }

    fun provideUpdateUserStringFieldUseCase(settingsRepository: SettingsRepository) : UpdateUserStringFieldUseCase {
        return UpdateUserStringFieldUseCase(settingsRepository)
    }
}
