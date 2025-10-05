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
import com.minhtu.firesocialmedia.data.repository.LocalRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.NewsRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.NotificationRepositoryImpl
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
import com.minhtu.firesocialmedia.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
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
import com.minhtu.firesocialmedia.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.DeleteNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetLatestNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SaveNewToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.UpdateNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.GetAllNotificationOfUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.showimage.DownloadImageUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.domain.usecases.signup.SignUpUseCase
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

object AppModule {
    fun provideSaveNotificationToDatabaseUseCase(notificationRepository: NotificationRepository) : SaveNotificationToDatabaseUseCase{
        return SaveNotificationToDatabaseUseCase(notificationRepository)
    }
    fun provideDeleteNotificationFromDatabaseUseCase(notificationRepository: NotificationRepository) : DeleteNotificationFromDatabaseUseCase{
        return DeleteNotificationFromDatabaseUseCase(notificationRepository)
    }
    fun provideSaveValueToDatabaseUseCase(commonDbRepository: CommonDbRepository) : SaveLikedPostUseCase {
        return SaveLikedPostUseCase(commonDbRepository)
    }
    fun provideSaveLikedCommentsUseCase(commonDbRepository: CommonDbRepository) : SaveLikedCommentsUseCase {
        return SaveLikedCommentsUseCase(commonDbRepository)
    }
    fun provideNotificationRepository(platformContext: PlatformContext) : NotificationRepository {
        return NotificationRepositoryImpl(platformContext.database)
    }
    fun provideCommonDbRepository(platformContext: PlatformContext) : CommonDbRepository{
        return CommonDbRepositoryImpl(platformContext.database)
    }
    //---------------------------Sign in----------------------------------------//
    fun provideSignInUseCase(authenticationRepository: AuthenticationRepository) : SignInUseCase {
        return SignInUseCase(authenticationRepository)
    }
    fun provideRememberPasswordUseCase(authenticationRepository: AuthenticationRepository) : RememberPasswordUseCase {
        return RememberPasswordUseCase(authenticationRepository)
    }
    fun provideCheckUserExistsUseCase(authenticationRepository: AuthenticationRepository) : CheckUserExistsUseCase {
        return CheckUserExistsUseCase(authenticationRepository)
    }
    fun provideCheckLocalAccountUseCase(authenticationRepository: AuthenticationRepository) : CheckLocalAccountUseCase {
        return CheckLocalAccountUseCase(authenticationRepository)
    }
    fun provideHandleSignInGoogleResultUseCase(authenticationRepository: AuthenticationRepository) : HandleSignInGoogleResultUseCase {
        return HandleSignInGoogleResultUseCase(authenticationRepository)
    }
    fun provideSignInViewModel(signInUseCase : SignInUseCase,
                               rememberPasswordUseCase: RememberPasswordUseCase,
                               checkUserExistsUseCase: CheckUserExistsUseCase,
                               checkLocalAccountUseCase : CheckLocalAccountUseCase,
                               handleSignInGoogleResult: HandleSignInGoogleResultUseCase) : SignInViewModel{
        return SignInViewModel(
            signInUseCase,
            rememberPasswordUseCase,
            checkUserExistsUseCase,
            checkLocalAccountUseCase,
            handleSignInGoogleResult
        )
    }

    fun provideAuthenticationRepository(platformContext: PlatformContext) : AuthenticationRepository{
        return AuthenticationRepositoryImpl(platformContext.auth, platformContext.database, platformContext.crypto)
    }

    //---------------------------Sign up----------------------------------------//
    fun provideSignUpRepository(platformContext: PlatformContext) : AuthenticationRepository {
        return AuthenticationRepositoryImpl(platformContext.auth, platformContext.database, platformContext.crypto)
    }
    fun provideSignUpUseCase(authenticationRepository: AuthenticationRepository) : SignUpUseCase {
        return SignUpUseCase(authenticationRepository)
    }
    fun provideSignUpViewModel(signUpUseCase: SignUpUseCase): SignUpViewModel {
        return SignUpViewModel(signUpUseCase)
    }

    //---------------------------Forgot password----------------------------------------//
    fun provideForgotPasswordRepository(platformContext: PlatformContext) : AuthenticationRepository {
        return AuthenticationRepositoryImpl(platformContext.auth, platformContext.database, platformContext.crypto)
    }
    fun provideCheckIfEmailExistsUseCase(authenticationRepository: AuthenticationRepository) : CheckIfEmailExistsUseCase{
        return CheckIfEmailExistsUseCase(authenticationRepository)
    }
    fun provideSendEmailResetPasswordUseCase(authenticationRepository: AuthenticationRepository) : SendEmailResetPasswordUseCase {
        return SendEmailResetPasswordUseCase(authenticationRepository)
    }
    fun provideForgotPasswordViewModel(
        checkIfEmailExistsUseCase: CheckIfEmailExistsUseCase,
        sendEmailResetPasswordUseCase : SendEmailResetPasswordUseCase
        ): ForgotPasswordViewModel {
        return ForgotPasswordViewModel(
            checkIfEmailExistsUseCase,
            sendEmailResetPasswordUseCase)
    }

    //---------------------------Information----------------------------------------//
    fun provideInformationRepository(platformContext: PlatformContext) : AuthenticationRepository {
        return AuthenticationRepositoryImpl(platformContext.auth, platformContext.database, platformContext.crypto)
    }
    fun provideLocalRepository(platformContext: PlatformContext) : LocalRepository {
        return LocalRepositoryImpl(platformContext.crypto)
    }
    fun provideSaveSignUpInformationUseCase(authenticationRepository: AuthenticationRepository) : SaveSignUpInformationUseCase {
        return SaveSignUpInformationUseCase(authenticationRepository)
    }
    fun provideGetFCMTokenUseCase(localRepository: LocalRepository) : GetFCMTokenUseCase {
        return GetFCMTokenUseCase(localRepository)
    }
    fun provideInformationViewModel(
        saveSignUpInformationUseCase: SaveSignUpInformationUseCase,
        getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
        getFCMTokenUseCase: GetFCMTokenUseCase
    ) : InformationViewModel {
        return InformationViewModel(
            saveSignUpInformationUseCase,
            getCurrentUserUidUseCase,
            getFCMTokenUseCase)
    }

    //---------------------------Loading----------------------------------------//
    fun provideLoadingViewModel() : LoadingViewModel {
        return LoadingViewModel()
    }
    //---------------------------Home----------------------------------------//
    fun provideGetCurrentUserUidUseCase(userRepository: UserRepository) : GetCurrentUserUidUseCase {
        return GetCurrentUserUidUseCase(userRepository)
    }
    fun provideGetLatestNewsUseCase(newsRepository: NewsRepository) : GetLatestNewsUseCase {
        return GetLatestNewsUseCase(newsRepository)
    }
    fun provideGetAllNotificationOfUserUseCase(notificationRepository: NotificationRepository) : GetAllNotificationOfUserUseCase {
        return GetAllNotificationOfUserUseCase(notificationRepository)
    }
    fun provideUpdateFCMTokenUseCase(userRepository: UserRepository) : UpdateFCMTokenUseCase {
        return UpdateFCMTokenUseCase(userRepository)
    }
    fun provideClearAccountUseCase(homeRepository: AuthenticationRepository) : ClearAccountUseCase {
        return ClearAccountUseCase(homeRepository)
    }
    fun provideUpdateCountValueInDatabase(commonDbRepository: CommonDbRepository) : UpdateLikeCountForNewUseCase {
        return UpdateLikeCountForNewUseCase(commonDbRepository)
    }
    fun provideDeleteNewsFromDatabaseUseCase(newsRepository: NewsRepository) : DeleteNewsFromDatabaseUseCase {
        return DeleteNewsFromDatabaseUseCase(newsRepository)
    }
    fun provideObservePhoneCallWithInCallUseCase(sendSignalingDataUseCase: SendSignalingDataUseCase) : ObservePhoneCallWithInCallUseCase {
        return ObservePhoneCallWithInCallUseCase(sendSignalingDataUseCase)
    }
    fun provideStopObservePhoneCallUseCase(sendSignalingDataUseCase: SendSignalingDataUseCase) : StopObservePhoneCallUseCase {
        return StopObservePhoneCallUseCase(sendSignalingDataUseCase)
    }
    fun provideStopCallServiceUseCase(callRepository: CallRepository) : StopCallServiceUseCase {
        return StopCallServiceUseCase(callRepository)
    }
    fun provideSearchUserByNameUseCase(userRepository: UserRepository) : SearchUserByNameUseCase {
        return SearchUserByNameUseCase(userRepository)
    }
    fun provideSendSignalingDataUseCase(callRepository: CallRepository) : SendSignalingDataUseCase {
        return SendSignalingDataUseCase(callRepository)
    }
    fun provideUserInteractor(
        getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
        getUserUseCase: GetUserUseCase,
        updateFCMTokenUseCase: UpdateFCMTokenUseCase,
        clearAccountUseCase: ClearAccountUseCase,
        saveValueToDatabaseUseCase: SaveLikedPostUseCase,
        searchUserByNameUseCase: SearchUserByNameUseCase
    ) : UserInteractor {
        return UserInteractorImpl(
            getCurrentUserUidUseCase,
            getUserUseCase,
            updateFCMTokenUseCase,
            clearAccountUseCase,
            saveValueToDatabaseUseCase,
            searchUserByNameUseCase
        )
    }
    fun provideNewsInteractor(
        getLatestNewsUseCase: GetLatestNewsUseCase,
        updateCountValueInDatabase: UpdateLikeCountForNewUseCase,
        deleteNewsFromDatabaseUseCase: DeleteNewsFromDatabaseUseCase
    ) : NewsInteractor {
        return NewsInteractorImpl(
            getLatestNewsUseCase,
            updateCountValueInDatabase,
            deleteNewsFromDatabaseUseCase
        )
    }
    fun provideNotificationInteractor(
        getAllNotificationOfUserUseCase: GetAllNotificationOfUserUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        deleteNotificationFromDatabaseUseCase: DeleteNotificationFromDatabaseUseCase
    ) : NotificationInteractor {
        return NotificationInteractorImpl(
            getAllNotificationOfUserUseCase,
            saveNotificationToDatabaseUseCase,
            deleteNotificationFromDatabaseUseCase
        )
    }
    fun provideCallInteractor(
        observePhoneCallWithInCallUseCase: ObservePhoneCallWithInCallUseCase,
        stopObservePhoneCallUseCase: StopObservePhoneCallUseCase,
        stopCallServiceUseCase: StopCallServiceUseCase
    ) : CallInteractor {
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
    ) : HomeViewModel {
        return HomeViewModel(
            userInteractor,
            newsInteractor,
            notificationInteractor,
            callInteractor
            )
    }

    //---------------------------Comment----------------------------------------//
    fun provideCommentRepository(platformContext: PlatformContext) : CommentRepository {
        return CommentRepositoryImpl(platformContext.database)
    }
    fun provideSaveCommentToDatabaseUseCase(commonDbRepository: CommonDbRepository) : SaveCommentToDatabaseUseCase {
        return SaveCommentToDatabaseUseCase(commonDbRepository)
    }
    fun provideSaveSubCommentToDatabaseUseCase(commonDbRepository: CommonDbRepository) : SaveSubCommentToDatabaseUseCase {
        return SaveSubCommentToDatabaseUseCase(commonDbRepository)
    }
    fun provideDeleteCommentFromDatabaseUseCase(commonDbRepository: CommonDbRepository) : DeleteCommentFromDatabaseUseCase {
        return DeleteCommentFromDatabaseUseCase(commonDbRepository)
    }
    fun provideDeleteSubCommentFromDatabaseUseCase(commonDbRepository: CommonDbRepository) : DeleteSubCommentFromDatabaseUseCase {
        return DeleteSubCommentFromDatabaseUseCase(commonDbRepository)
    }
    fun provideGetAllCommentsUseCase(commentRepository: CommentRepository) : GetAllCommentsUseCase {
        return GetAllCommentsUseCase(commentRepository)
    }
    fun provideUpdateCommentCountForNewUseCase(commonDbRepository: CommonDbRepository) : UpdateCommentCountForNewUseCase {
        return UpdateCommentCountForNewUseCase(commonDbRepository)
    }
    fun provideUpdateReplyCountForCommentUseCase(commonDbRepository: CommonDbRepository) : UpdateReplyCountForCommentUseCase {
        return UpdateReplyCountForCommentUseCase(commonDbRepository)
    }
    fun provideCommentInteractor(
        saveCommentToDatabaseUseCase: SaveCommentToDatabaseUseCase,
        saveSubCommentToDatabaseUseCase: SaveSubCommentToDatabaseUseCase,
        deleteCommentFromDatabaseUseCase: DeleteCommentFromDatabaseUseCase,
        deleteSubCommentFromDatabaseUseCase: DeleteSubCommentFromDatabaseUseCase,
        getAllCommentsUseCase: GetAllCommentsUseCase
    ) : CommentInteractor {
        return CommentInteractorImpl(
            saveCommentToDatabaseUseCase,
            saveSubCommentToDatabaseUseCase,
            deleteCommentFromDatabaseUseCase,
            deleteSubCommentFromDatabaseUseCase,
            getAllCommentsUseCase
        )
    }
    fun provideUpdateLikeCountForCommentUseCase(commonDbRepository: CommonDbRepository) : UpdateLikeCountForCommentUseCase {
        return UpdateLikeCountForCommentUseCase(commonDbRepository)
    }
    fun provideUpdateLikeCountForSubCommentUseCase(commonDbRepository: CommonDbRepository) : UpdateLikeCountForSubCommentUseCase {
        return UpdateLikeCountForSubCommentUseCase(commonDbRepository)
    }
    fun provideCommentViewModel(
        commentInteractor: CommentInteractor,
        getUserUseCase : GetUserUseCase,
        saveLikedCommentsUseCase : SaveLikedCommentsUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        updateCommentCountForNewUseCase: UpdateCommentCountForNewUseCase,
        updateReplyCountForCommentUseCase: UpdateReplyCountForCommentUseCase,
        updateLikeCountForCommentUseCase: UpdateLikeCountForCommentUseCase,
        updateLikeCountForSubCommentUseCase: UpdateLikeCountForSubCommentUseCase
    ) : CommentViewModel {
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
    fun provideSaveNewToDatabaseUseCase(commonDbRepository: CommonDbRepository) : SaveNewToDatabaseUseCase {
        return SaveNewToDatabaseUseCase(commonDbRepository)
    }
    fun provideUpdateNewsFromDatabaseUseCase(newsRepository: NewsRepository) : UpdateNewsFromDatabaseUseCase {
        return UpdateNewsFromDatabaseUseCase(newsRepository)
    }
    fun provideUploadNewfeedViewModel(
        getUserUseCase: GetUserUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        saveNewToDatabaseUseCase: SaveNewToDatabaseUseCase,
        updateNewsFromDatabaseUseCase: UpdateNewsFromDatabaseUseCase): UploadNewfeedViewModel {
        return UploadNewfeedViewModel(
            getUserUseCase,
            saveNotificationToDatabaseUseCase,
            saveNewToDatabaseUseCase,
            updateNewsFromDatabaseUseCase
        )
    }

    //---------------------------User Information----------------------------------------//
    fun provideCallRepository(platformContext: PlatformContext) : CallRepository {
        return CallRepositoryImpl(
            platformContext.audioCall,
            platformContext.database,
            platformContext.audioCall,
            platformContext.permissionManager)
    }
    fun provideCheckCalleeAvailableUseCase(callRepository: CallRepository) : CheckCalleeAvailableUseCase{
        return CheckCalleeAvailableUseCase(callRepository)
    }
    fun provideUserInformationViewModel(
        saveFriendUseCase: SaveFriendUseCase,
        saveFriendRequestUseCase: SaveFriendRequestUseCase,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
        checkCalleeAvailableUseCase: CheckCalleeAvailableUseCase): UserInformationViewModel {
        return UserInformationViewModel(
            saveFriendUseCase,
            saveFriendRequestUseCase,
            saveNotificationToDatabaseUseCase,
            checkCalleeAvailableUseCase
        )
    }

    //---------------------------Friend----------------------------------------//
    fun provideSaveFriendUseCase(commonDbRepository: CommonDbRepository) : SaveFriendUseCase{
        return SaveFriendUseCase(commonDbRepository)
    }
    fun provideSaveFriendRequestUseCase(commonDbRepository: CommonDbRepository) : SaveFriendRequestUseCase{
        return SaveFriendRequestUseCase(commonDbRepository)
    }
    fun provideFriendViewModel(
        saveFriendUseCase: SaveFriendUseCase,
        saveFriendRequestUseCase: SaveFriendRequestUseCase) : FriendViewModel {
        return FriendViewModel(
            saveFriendUseCase,
            saveFriendRequestUseCase)
    }

    //---------------------------Search----------------------------------------//
    fun provideSeachViewModel(): SearchViewModel {
        return SearchViewModel()
    }

    //---------------------------Show Image----------------------------------------//
    fun provideShowImageRepository(platformContext: PlatformContext) : ShowImageRepository {
        return ShowImageRepositoryImpl(platformContext.database)
    }
    fun provideDownloadImageUseCase(showImageRepository: ShowImageRepository) : DownloadImageUseCase {
        return DownloadImageUseCase(showImageRepository)
    }
    fun provideShowImageViewModel(downloadImageUseCase: DownloadImageUseCase): ShowImageViewModel {
        return ShowImageViewModel(downloadImageUseCase)
    }

    //---------------------------Notification----------------------------------------//
    fun provideUserRepository(platformContext: PlatformContext) : UserRepository {
        return UserRepositoryImpl(platformContext.auth,platformContext.database)
    }
    fun provideNewsRepository(platformContext: PlatformContext) : NewsRepository {
        return NewsRepositoryImpl(platformContext.database)
    }
    fun provideGetUserUseCase(userRepository: UserRepository) : GetUserUseCase {
        return GetUserUseCase(userRepository)
    }
    fun provideFindNewByIdInDbUseCase(newsRepository: NewsRepository) : FindNewByIdInDbUseCase {
        return FindNewByIdInDbUseCase(newsRepository)
    }
    fun provideNotificationViewModel(
        getUserUseCase: GetUserUseCase,
        findNewByIdInDbUseCase: FindNewByIdInDbUseCase) : NotificationViewModel {
        return NotificationViewModel(
            getUserUseCase,
            findNewByIdInDbUseCase
        )
    }
    //---------------------------Call----------------------------------------//
    fun provideStartCallServiceUseCase(callRepository: CallRepository) : StartCallServiceUseCase{
        return StartCallServiceUseCase(callRepository)
    }

    fun provideManageCallStateUseCase(callRepository: CallRepository) : ManageCallStateUseCase{
        return ManageCallStateUseCase(callRepository)
    }

    fun provideRequestPermissionUseCase(callRepository: CallRepository) : RequestPermissionUseCase{
        return RequestPermissionUseCase(callRepository)
    }

    fun provideStartVideoCallServiceUseCase(callRepository: CallRepository) : StartVideoCallServiceUseCase {
        return StartVideoCallServiceUseCase(callRepository)
    }

    fun provideRequestCameraAndAudioPermissionsUseCase(callRepository: CallRepository) : RequestCameraAndAudioPermissionsUseCase {
        return RequestCameraAndAudioPermissionsUseCase(callRepository)
    }

    fun provideInitializeCallUseCase(callRepository: CallRepository) : InitializeCallUseCase {
        return InitializeCallUseCase(callRepository)
    }

    fun provideVideoCallUseCase(callRepository: CallRepository) : VideoCallUseCase {
        return VideoCallUseCase(callRepository)
    }
}