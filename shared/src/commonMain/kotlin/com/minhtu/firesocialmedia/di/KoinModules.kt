package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.core.application.interactor.CallInteractorImpl
import com.minhtu.firesocialmedia.core.application.interactor.CommentInteractorImpl
import com.minhtu.firesocialmedia.core.application.interactor.NewsInteractorImpl
import com.minhtu.firesocialmedia.core.application.interactor.NotificationInteractorImpl
import com.minhtu.firesocialmedia.core.application.interactor.UserInteractorImpl
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
import com.minhtu.firesocialmedia.core.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.core.domain.interactor.home.CallInteractor
import com.minhtu.firesocialmedia.core.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.core.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.core.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.core.domain.repository.CallRepository
import com.minhtu.firesocialmedia.core.domain.repository.CommentRepository
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.repository.LocalRepository
import com.minhtu.firesocialmedia.core.domain.repository.NetworkRepository
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.core.domain.repository.ShowImageRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.call.AcceptCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.AddIceCandidatesUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.CreateOfferUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.EndCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.ListenForIncomingCallsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.ObserveAnswer
import com.minhtu.firesocialmedia.core.domain.usecases.call.ObserveCallStatus
import com.minhtu.firesocialmedia.core.domain.usecases.call.ObserveIceCandidateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.ObservePhoneCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.ObservePhoneCallWithInCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.ObserveVideoCall
import com.minhtu.firesocialmedia.core.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.SendAnswerUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.SendIceCandidateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.SendOfferUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.SendSignalingDataUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.SendWhoEndCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.SetRemoteDescriptionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StartCallServiceUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StartCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StopCallServiceUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StopObservePhoneCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateCameraStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateMicStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateSpeakerStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.DeleteCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.DeleteSubCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.GetAllCommentsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.SaveCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.SaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.SaveSubCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchRecommendGroupsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FindGroupInformationUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.GetAllGroupsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.GetAllMembersInGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.GetGroupConfigsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.InviteFriendToGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.SaveNewToGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.UpdateNotificationStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.ClearLocalDataUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.ClearLocalFriendsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.DeleteNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.GetLatestNewsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.SaveCurrentUserInfoUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.StoreNewsToRoomUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.StoreNotificationsToRoomUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.StoreUserFriendsToRoomUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.UpdateFCMTokenUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.network.CheckInternetConnectionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.DeleteAllDraftPostsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.DeleteDraftPostUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.DeletePollUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.FetchPollUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.LoadAllVotersUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.LoadMyVotesUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.SaveNewToDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.SubmitVoteUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.UpdateNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.DeleteAllNotificationsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.GetAllNotificationOfUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.UpdateIsReadStatusOfNotificationUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.BuildOtpAuthUrlUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.ChangePasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.CreatePollUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.DeleteLoginSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Disable2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Enable2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.GenerateSecretFor2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Get2FAVerifiedStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.LogoutSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.ObserveSessionStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.StopObserveSessionStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserAvatarUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserBackgroundUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserStringFieldUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserTimestampUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateVerify2FASuccessUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.ValidateNewPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Verify2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyBackupCodeUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.showimage.DownloadImageUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.sync.LoadNewsPostedWhenOfflineUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.sync.SyncDataUseCase
import com.minhtu.firesocialmedia.presentation.calling.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.presentation.calling.videocall.VideoCallViewModel
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.RouterViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.notification.NotificationViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.CreateGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.CreatePollViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ExploreGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.GroupDetailsViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.InviteMemberViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembersViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.PollViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.SelectGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.notificationconfigs.NotificationConfigsViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.SecuritySettingsViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.changepassword.ChangePasswordViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.loginhistory.LoginHistoryViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.BackUpCodeViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.TwoFAViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.TwoFactorEnabledViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.VerifyOTPViewModel
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformationViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.showimage.ShowImageViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun appModule() = module {
    // ── Platform context as a singleton ────────────────────//
    single<PlatformContext> { PlatformContextHolder.instance }

    // ── Repositories (singletons) ────────────────────//
    single<CallRepository> {
        CallRepositoryImpl(
            { get<PlatformContext>().audioCall },
            get<PlatformContext>().database,
            get<PlatformContext>().permissionManager
        )
    }

    single<CommentRepository> {
        CommentRepositoryImpl(
            get<PlatformContext>().database
        )
    }

    single<CommonDbRepository> {
        CommonDbRepositoryImpl(
            get<PlatformContext>().database,
            get<PlatformContext>().room,
            get<PlatformContext>().networkMonitor,
            get<PlatformContext>().ipRemoteDataSource
        )
    }

    single<GroupRepository> {
        GroupRepositoryImpl(
            get<PlatformContext>().database,
            get<PlatformContext>().networkMonitor,
            get<PlatformContext>().clipboard
        )
    }

    single<LocalRepository> {
        LocalRepositoryImpl(
            get<PlatformContext>().crypto,
            get<PlatformContext>().room
        )
    }

    single<NetworkRepository> {
        NetworkRepositoryImpl(
            get<PlatformContext>().networkMonitor
        )
    }

    single<NewsRepository> {
        NewsRepositoryImpl(
            get<PlatformContext>().database,
            get<PlatformContext>().room,
            get<PlatformContext>().networkMonitor
        )
    }

    single<NotificationRepository> {
        NotificationRepositoryImpl(
            get<PlatformContext>().database,
            get<PlatformContext>().room,
            get<PlatformContext>().networkMonitor
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get<PlatformContext>().auth,
            get<PlatformContext>().database,
            get<PlatformContext>().clipboard,
            get<PlatformContext>().crypto
        )
    }

    single<ShowImageRepository> {
        ShowImageRepositoryImpl(
            get<PlatformContext>().database
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(
            get<PlatformContext>().auth,
            get<PlatformContext>().database,
            get<PlatformContext>().crypto,
            get<PlatformContext>().room,
            get<PlatformContext>().networkMonitor
        )
    }
    // ── Use Cases (factory = new instance each time) ────────────────────//
    // Calling Use Cases
    factory { InitializeCallUseCase(get()) }
    factory { ManageCallStateUseCase(get()) }
    factory { RequestCameraAndAudioPermissionsUseCase(get()) }
    factory { RequestPermissionUseCase(get()) }
    factory { SendSignalingDataUseCase(get()) }
    factory { StartCallServiceUseCase(get()) }
    factory { StartVideoCallServiceUseCase(get()) }
    factory { StopCallServiceUseCase(get()) }
    factory { UpdateCameraStatusUseCase(get()) }
    factory { UpdateMicStatusUseCase(get()) }
    factory { UpdateSpeakerStatusUseCase(get()) }
    factory { VideoCallUseCase(get()) }

    // Callee Use Cases
    factory { ListenForIncomingCallsUseCase(get()) }
    factory { ObservePhoneCallUseCase(get()) }
    factory { ObservePhoneCallWithInCallUseCase(get()) }
    factory { StopObservePhoneCallUseCase(get()) }
    factory { SetRemoteDescriptionUseCase(get()) }
    factory { SendAnswerUseCase(get()) }
    factory { AcceptCallUseCase(get()) }
    factory { AddIceCandidatesUseCase(get()) }
    factory { SendWhoEndCallUseCase(get()) }

    // Caller Use Cases
    factory { StartCallUseCase(get(), get(), get()) }
    factory { SendOfferUseCase(get()) }
    factory { CreateOfferUseCase(get()) }
    factory { SendIceCandidateUseCase(get()) }
    factory { ObserveIceCandidateUseCase(get()) }
    factory { ObserveAnswer(get()) }
    factory { ObserveCallStatus(get()) }
    factory { ObserveVideoCall(get()) }
    factory { EndCallUseCase(get()) }

    // Comment Use Cases
    factory { DeleteCommentFromDatabaseUseCase(get()) }
    factory { DeleteSubCommentFromDatabaseUseCase(get()) }
    factory { GetAllCommentsUseCase(get()) }
    factory { SaveCommentToDatabaseUseCase(get()) }
    factory { SaveLikedCommentsUseCase(get()) }
    factory { SaveSubCommentToDatabaseUseCase(get()) }
    factory { UpdateCommentCountForNewUseCase(get()) }
    factory { UpdateLikeCountForCommentUseCase(get()) }
    factory { UpdateLikeCountForSubCommentUseCase(get()) }
    factory { UpdateReplyCountForCommentUseCase(get()) }

    // Common Use Cases
    factory { GetCurrentUserUidUseCase(get()) }
    factory { GetFCMTokenUseCase(get()) }
    factory { GetUserUseCase(get()) }

    // Friend Use Cases
    factory { SaveFriendRequestUseCase(get()) }
    factory { SaveFriendUseCase(get()) }

    // Group Use Cases
    factory { CopyLinkUseCase(get()) }
    factory { CreateGroupUseCase(get()) }
    factory { DemoteMemberUseCase(get()) }
    factory { FetchFeatureGroupsUseCase(get()) }
    factory { FetchGroupInfoUseCase(get()) }
    factory { FetchNotificationStateUseCase(get()) }
    factory { FetchRecommendGroupsUseCase(get()) }
    factory { FindGroupByIdUseCase(get()) }
    factory { FindGroupInformationUseCase(get()) }
    factory { GetAllGroupsUseCase(get()) }
    factory { GetAllMembersInGroupUseCase(get()) }
    factory { GetGroupConfigsUseCase(get()) }
    factory { InviteFriendToGroupUseCase(get()) }
    factory { JoinGroupUseCase(get()) }
    factory { LeaveAndDeleteGroupUseCase(get()) }
    factory { LeaveGroupUseCase(get()) }
    factory { PromoteMemberUseCase(get()) }
    factory { RemoveMemberUseCase(get()) }
    factory { SaveNewToGroupUseCase(get()) }
    factory { UpdateNotificationStatusUseCase(get()) }

    // Home Use Cases
    factory { ClearAccountUseCase(get()) }
    factory { ClearLocalDataUseCase(get()) }
    factory { ClearLocalFriendsUseCase(get()) }
    factory { DeleteNewsFromDatabaseUseCase(get()) }
    factory { GetLatestNewsUseCase(get()) }
    factory { SaveCurrentUserInfoUseCase(get()) }
    factory { SaveLikedPostUseCase(get()) }
    factory { SearchUserByNameUseCase(get()) }
    factory { StoreNewsToRoomUseCase(get()) }
    factory { StoreNotificationsToRoomUseCase(get()) }
    factory { StoreUserFriendsToRoomUseCase(get()) }
    factory { UpdateFCMTokenUseCase(get()) }
    factory { UpdateLikeCountForNewUseCase(get()) }

    // Information Use Cases
    factory { CheckCalleeAvailableUseCase(get()) }
    factory { SaveSignUpInformationUseCase(get()) }

    // Network Use Cases
    factory { CheckInternetConnectionUseCase(get()) }

    // Newsfeed Use Cases
    factory { DeleteAllDraftPostsUseCase(get()) }
    factory { DeleteDraftPostUseCase(get()) }
    factory { DeletePollUseCase(get()) }
    factory { FetchPollUseCase(get()) }
    factory { LoadAllVotersUseCase(get()) }
    factory { LoadMyVotesUseCase(get()) }
    factory { SaveNewToDatabaseUseCase(get()) }
    factory { SubmitVoteUseCase(get()) }
    factory { UpdateNewsFromDatabaseUseCase(get()) }

    // Notification Use Cases
    factory { DeleteAllNotificationsUseCase(get()) }
    factory { DeleteNotificationFromDatabaseUseCase(get()) }
    factory { FindNewByIdInDbUseCase(get()) }
    factory { GetAllNotificationOfUserUseCase(get()) }
    factory { SaveNotificationToDatabaseUseCase(get()) }
    factory { UpdateIsReadStatusOfNotificationUseCase(get()) }

    // Settings Use Cases
    factory { BuildOtpAuthUrlUseCase() }
    factory { ChangePasswordUseCase(get()) }
    factory { CopyUseCase(get()) }
    factory { CreatePollUseCase(get()) }
    factory { DeleteLoginSessionUseCase(get()) }
    factory { Disable2FAUseCase(get()) }
    factory { Enable2FAUseCase(get()) }
    factory { FetchLoginHistoryListUseCase(get()) }
    factory { GenerateSecretFor2FAUseCase(get()) }
    factory { Get2FAVerifiedStatusUseCase(get()) }
    factory { LogoutSessionUseCase(get()) }
    factory { ObserveSessionStatusUseCase(get()) }
    factory { StopObserveSessionStatusUseCase(get()) }
    factory { UpdateUserAvatarUseCase(get()) }
    factory { UpdateUserBackgroundUseCase(get()) }
    factory { UpdateUserStringFieldUseCase(get()) }
    factory { UpdateUserTimestampUseCase(get()) }
    factory { UpdateVerify2FASuccessUseCase(get()) }
    factory { ValidateNewPasswordUseCase() }
    factory { Verify2FAUseCase(get()) }
    factory { VerifyBackupCodeUseCase(get()) }
    factory { VerifyCurrentPasswordUseCase(get()) }

    // Show image Use Cases
    factory { DownloadImageUseCase(get()) }

    // Sync Use Cases
    factory { LoadNewsPostedWhenOfflineUseCase(get()) }
    factory { SyncDataUseCase(get()) }

    // ── Interactors ────────────────────//
    factory<UserInteractor> {
        UserInteractorImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory<NewsInteractor> {
        NewsInteractorImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory<NotificationInteractor> {
        NotificationInteractorImpl(
            get(),
            get(),
            get(),
            get()
        )
    }

    factory<CallInteractor> {
        CallInteractorImpl(
            get(),
            get(),
            get()
        )
    }

    factory<CommentInteractor> {
        CommentInteractorImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    // ── ViewModels ────────────────────//
    viewModel { CallingViewModel(get(), get(), get()) }
    viewModel { VideoCallViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { CommentViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    // HomeViewModel registration moved to feature:home homeModule()
    viewModel { LoadingViewModel() }
    viewModel { RouterViewModel(get(), get(), get(), get()) }
    viewModel { FriendViewModel(get(), get()) }
    viewModel { NotificationViewModel(get(), get(), get(), get()) }
    viewModel { CreateGroupViewModel(get()) }
    viewModel { CreatePollViewModel(get()) }
    viewModel { ExploreGroupViewModel(get(), get()) }
    viewModel { GroupDetailsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { InviteMemberViewModel(get(), get(), get()) }
    viewModel { ManageMembersViewModel(get(), get(), get(), get()) }
    viewModel { PollViewModel(get(), get(), get(), get()) }
    viewModel { SelectGroupViewModel(get()) }
    viewModel { NotificationConfigsViewModel() }
    viewModel { ChangePasswordViewModel(get(), get(), get()) }
    viewModel { LoginHistoryViewModel(get(), get(), get(), get(), get()) }
    viewModel { BackUpCodeViewModel(get(), get()) }
    viewModel { TwoFactorEnabledViewModel(get(), get()) }
    viewModel { TwoFAViewModel(get(), get(), get()) }
    viewModel { VerifyOTPViewModel(get(), get(), get()) }
    viewModel { SecuritySettingsViewModel(get()) }
    viewModel { PostInformationViewModel(get()) }
    viewModel { SearchViewModel() }
    viewModel { ShowImageViewModel(get()) }
    // UploadNewfeedViewModel registration moved to feature:home homeModule()
    // UserInformationViewModel registration moved to feature:profile profileModule()
    // PersonalInformationViewModel registration moved to feature:profile profileModule()
    // InformationViewModel registration moved to feature:auth authModule()
}