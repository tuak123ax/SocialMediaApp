package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.di.AppModule
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.presentation.calling.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.presentation.calling.videocall.VideoCallViewModel
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.NotificationViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.CreateGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ExploreGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.GroupDetailsViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.InviteMemberViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembersViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.SelectGroupViewModel
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformationViewModel
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
        val localRepository = AppModule.provideLocalRepository(platformContext)
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
        val storeUserFriendsToRoomUseCase = AppModule.provideStoreUserFriendsToRoomUseCase(localRepository)
        val storeNewsToRoomUseCase = AppModule.provideStoreNewsToRoomUseCase(localRepository)
        val storeNotificationsToRoomUseCase = AppModule.provideStoreNotificationsToRoomUseCase(localRepository)
        val saveCurrentUserInfoUseCase = AppModule.provideSaveCurrentUserInfoUseCase(localRepository)
        val clearLocalDataUseCase = AppModule.provideClearLocalDataUseCase(commonDbRepository)
        val saveNewToDatabaseUseCase = AppModule.provideSaveNewToDatabaseUseCase(commonDbRepository)
        val findNewByIdInDbUseCase = AppModule.provideFindNewByIdInDbUseCase(newsRepository)
        val clearLocalFriendsUseCase = AppModule.provideClearLocalFriendsUseCase(commonDbRepository)
        val userInteractor = AppModule.provideUserInteractor(
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
        val newsInteractor = AppModule.provideNewsInteractor(
            getLatestNewsUseCase,
            updateCountValueInDatabase,
            deleteNewsFromDatabaseUseCase,
            storeNewsToRoomUseCase,
            saveNewToDatabaseUseCase,
            findNewByIdInDbUseCase
        )
        val notificationInteractor = AppModule.provideNotificationInteractor(
            getAllNotificationOfUserUseCase,
            saveNotificationToDatabaseUseCase,
            deleteNotificationFromDatabaseUseCase,
            storeNotificationsToRoomUseCase
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
        val groupRepository = AppModule.provideGroupRepository(platformContext)

        val saveNotificationToDatabaseUseCase = AppModule.provideSaveNotificationToDatabaseUseCase(notificationRepository)
        val getUserUseCase = AppModule.provideGetUserUseCase(userRepository)
        val saveNewToDatabaseUseCase = AppModule.provideSaveNewToDatabaseUseCase(commonDbRepository)
        val updateNewsFromDatabaseUseCase = AppModule.provideUpdateNewsFromDatabaseUseCase(newsRepository)
        val loadNewsPostedWhenOfflineUseCase = AppModule.provideLoadNewsPostedWhenOfflineUseCase(commonDbRepository)
        val deleteAllDraftPostsUseCase = AppModule.provideDeleteAllDraftPostsUseCase(commonDbRepository)
        val deleteDraftPostUseCase = AppModule.provideDeleteDraftPostUseCase(commonDbRepository)
        val saveNewToGroupUseCase = AppModule.provideSaveNewToGroupUseCase(groupRepository)
        val getAllMembersInGroupUseCase = AppModule.provideGetAllMembersInGroupUseCase(groupRepository)
        val getGroupConfigsUseCase = AppModule.provideGetGroupConfigsUseCase(groupRepository)
        return AppModule.provideUploadNewfeedViewModel(
            getUserUseCase,
            saveNotificationToDatabaseUseCase,
            saveNewToDatabaseUseCase,
            updateNewsFromDatabaseUseCase,
            loadNewsPostedWhenOfflineUseCase,
            deleteAllDraftPostsUseCase,
            deleteDraftPostUseCase,
            saveNewToGroupUseCase,
            getAllMembersInGroupUseCase,
            getGroupConfigsUseCase)
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

    fun createPostInformationViewModel(platformContext: PlatformContext): PostInformationViewModel {
        val newsRepository = AppModule.provideNewsRepository(platformContext)
        val findNewByIdInDbUseCase = AppModule.provideFindNewByIdInDbUseCase(newsRepository)
        return PostInformationViewModel(findNewByIdInDbUseCase)
    }

    fun createCreateGroupViewModel(platformContext: PlatformContext): CreateGroupViewModel {
        val groupRepository = AppModule.provideGroupRepository(platformContext)
        val createGroupUseCase = AppModule.provideCreateGroupUseCase(groupRepository)
        return CreateGroupViewModel(createGroupUseCase)
    }

    fun createGroupDetailsViewModel(platformContext: PlatformContext): GroupDetailsViewModel {
        val groupRepository = AppModule.provideGroupRepository(platformContext)
        val fetchGroupInfoUseCase = AppModule.provideFetchGroupInfoUseCase(groupRepository)
        val updateNotificationStatusUseCase = AppModule.provideUpdateNotificationStatusUseCase(groupRepository)
        val fetchNotificationStateUseCase = AppModule.provideFetchNotificationStateUseCase(groupRepository)
        val findGroupByIdUseCase = AppModule.provideFindGroupByIdUseCase(groupRepository)
        val joinGroupUseCase = AppModule.provideJoinGroupUseCase(groupRepository)
        val leaveGroupUseCase = AppModule.provideLeaveGroupUseCase(groupRepository)
        val leaveAndDeleteGroupUseCase = AppModule.provideLeaveAndDeleteGroupUseCase(groupRepository)
        return GroupDetailsViewModel(
            fetchGroupInfoUseCase,
            updateNotificationStatusUseCase,
            fetchNotificationStateUseCase,
            findGroupByIdUseCase,
            joinGroupUseCase,
            leaveGroupUseCase,
            leaveAndDeleteGroupUseCase)
    }

    fun createSelectGroupViewModel(platformContext: PlatformContext): SelectGroupViewModel {
        val groupRepository = AppModule.provideGroupRepository(platformContext)
        val getAllGroupsUseCase = AppModule.provideGetAllGroupsUseCase(groupRepository)
        return SelectGroupViewModel(
            getAllGroupsUseCase
        )
    }

    fun createInviteMemberViewModel(platformContext: PlatformContext): InviteMemberViewModel {
        val groupRepository = AppModule.provideGroupRepository(platformContext)
        val userRepository = AppModule.provideUserRepository(platformContext)
        val copyLinkUseCase = AppModule.provideCopyLinkUseCase(groupRepository)
        val getUserUseCase = AppModule.provideGetUserUseCase(userRepository)
        val inviteFriendToGroupUseCase = AppModule.provideInviteFriendToGroupUseCase(groupRepository)
        return InviteMemberViewModel(
            copyLinkUseCase,
            getUserUseCase,
            inviteFriendToGroupUseCase
            )
    }

    fun createManageMembersViewModel(platformContext: PlatformContext): ManageMembersViewModel {
        val userRepository = AppModule.provideUserRepository(platformContext)
        val groupRepository = AppModule.provideGroupRepository(platformContext)
        val getUserUseCase = AppModule.provideGetUserUseCase(userRepository)
        val removeMemberUseCase = AppModule.provideRemoveMemberUseCase(groupRepository)
        val promoteMemberUseCase = AppModule.providePromoteMemberUseCase(groupRepository)
        val demoteMemberUseCase = AppModule.provideDemoteMemberUseCase(groupRepository)
        return ManageMembersViewModel(
            getUserUseCase,
            removeMemberUseCase,
            promoteMemberUseCase,
            demoteMemberUseCase)
    }

    fun createExploreGroupViewModel(platformContext: PlatformContext): ExploreGroupViewModel {
        val groupRepository = AppModule.provideGroupRepository(platformContext)
        val fetchRecommendGroupsUseCase = AppModule.provideFetchRecommendGroupsUseCase(groupRepository)
        val fetchFeatureGroupsUseCase = AppModule.provideFetchFeatureGroupsUseCase((groupRepository))
        return ExploreGroupViewModel(
            fetchRecommendGroupsUseCase,
            fetchFeatureGroupsUseCase
        )
    }
}