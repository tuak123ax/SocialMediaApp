package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.network.group.NetworkMonitor
import com.minhtu.firesocialmedia.data.remote.service.auth.group.AuthSessionService
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.data.repository.GroupRepositoryImpl
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.data.repository.group.UserRepositoryImpl
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.data.repository.news.GroupNewsRepositoryImpl
import com.minhtu.firesocialmedia.data.remote.service.database.GroupDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.GroupStorageHelper
import com.minhtu.firesocialmedia.data.remote.service.clipboard.group.ClipboardService
import com.minhtu.firesocialmedia.domain.usecases.news.group.DeleteNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.GetNewByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchRecommendGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupInformationUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllMembersInGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetGroupConfigsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.SaveNewToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.UpdateNotificationStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.DeletePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.FetchPollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.LoadAllVotersUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.LoadMyVotesUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.SubmitVoteUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.CreatePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikeNotificationUseCase
import com.minhtu.firesocialmedia.presentation.creategroup.CreateGroupViewModel
import com.minhtu.firesocialmedia.presentation.createpoll.CreatePollViewModel
import com.minhtu.firesocialmedia.presentation.exploregroup.ExploreGroupViewModel
import com.minhtu.firesocialmedia.presentation.groupdetails.GroupDetailsViewModel
import com.minhtu.firesocialmedia.presentation.groupdetails.PollViewModel
import com.minhtu.firesocialmedia.presentation.invitemember.InviteMemberViewModel
import com.minhtu.firesocialmedia.presentation.managemembers.ManageMembersViewModel
import com.minhtu.firesocialmedia.presentation.selectgroup.SelectGroupViewModel
import com.minhtu.firesocialmedia.presentation.group.SessionViewModel
import com.minhtu.firesocialmedia.presentation.group.EngagementViewModel
import com.minhtu.firesocialmedia.group.presentation.loading.LoadingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun groupModule() = module {
    single<GroupRepository> {
        GroupRepositoryImpl(
            get<GroupDatabaseService>(),
            get<GroupStorageHelper>(),
            get<NetworkMonitor>(),
            get<ClipboardService>()
        )
    }
    single<GroupNewsRepository> {
        GroupNewsRepositoryImpl(
            get<GroupDatabaseService>(),
            get<NetworkMonitor>()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            get<AuthSessionService>(),
            get<GroupDatabaseService>(),
            get<NetworkMonitor>()
        )
    }
    factory { GetUserUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }
    factory { SaveLikedPostUseCase(get()) }
    factory { GetNewByIdUseCase(get()) }
    factory { DeleteNewsUseCase(get()) }
    factory { UpdateLikeCountForNewUseCase(get()) }

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
    factory { JoinGroupUseCase(get()) }
    factory { LeaveAndDeleteGroupUseCase(get()) }
    factory { LeaveGroupUseCase(get()) }
    factory { PromoteMemberUseCase(get()) }
    factory { RemoveMemberUseCase(get()) }
    factory { SaveNewToGroupUseCase(get()) }
    factory { UpdateNotificationStatusUseCase(get()) }

    // Poll use cases used within group context
    factory { DeletePollUseCase(get()) }
    factory { FetchPollUseCase(get()) }
    factory { LoadAllVotersUseCase(get()) }
    factory { LoadMyVotesUseCase(get()) }
    factory { SubmitVoteUseCase(get()) }
    factory { CreatePollUseCase(get()) }
    // SaveNotificationToDatabaseUseCase (2nd arg of SaveLikeNotificationUseCase) is bound by
    // feature:notification's own Koin module; resolved here via the flat app-level container
    // since group depends on feature:notification.
    factory { SaveLikeNotificationUseCase(get(), get()) }

    // Note: GroupNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.

    // ── ViewModels ────────────────────//
    viewModel { CreateGroupViewModel(get()) }
    viewModel { CreatePollViewModel(get()) }
    viewModel { ExploreGroupViewModel(get(), get()) }
    viewModel { GroupDetailsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PollViewModel(get(), get(), get(), get()) }
    // 3rd arg (SaveNotificationToDatabaseUseCase) resolves via feature:notification's Koin module.
    viewModel { InviteMemberViewModel(get(), get(), get(), get()) }
    viewModel { ManageMembersViewModel(get(), get(), get(), get(), get()) }
    viewModel { SelectGroupViewModel(get()) }
    viewModel { SessionViewModel(get(), get()) }
    viewModel { EngagementViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { LoadingViewModel() }
}
