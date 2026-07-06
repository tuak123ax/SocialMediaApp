package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.data.repository.GroupRepositoryImpl
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
import com.minhtu.firesocialmedia.navigation.GroupNavGraphImpl
import com.minhtu.firesocialmedia.presentation.creategroup.CreateGroupViewModel
import com.minhtu.firesocialmedia.presentation.createpoll.CreatePollViewModel
import com.minhtu.firesocialmedia.presentation.exploregroup.ExploreGroupViewModel
import com.minhtu.firesocialmedia.presentation.groupdetails.GroupDetailsViewModel
import com.minhtu.firesocialmedia.presentation.groupdetails.PollViewModel
import com.minhtu.firesocialmedia.presentation.invitemember.InviteMemberViewModel
import com.minhtu.firesocialmedia.presentation.managemembers.ManageMembersViewModel
import com.minhtu.firesocialmedia.presentation.selectgroup.SelectGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigation.GroupNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun groupModule() = module {
    single<GroupRepository> {
        GroupRepositoryImpl(
            get<PlatformContext>().database,
            get<PlatformContext>().networkMonitor,
            get<PlatformContext>().clipboard
        )
    }

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

    // Group navigation graph
    single<GroupNavGraph> { GroupNavGraphImpl() }

    // ── ViewModels ────────────────────//
    viewModel { CreateGroupViewModel(get()) }
    viewModel { CreatePollViewModel(get()) }
    viewModel { ExploreGroupViewModel(get(), get()) }
    viewModel { GroupDetailsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PollViewModel(get(), get(), get(), get()) }
    viewModel { InviteMemberViewModel(get(), get(), get()) }
    viewModel { ManageMembersViewModel(get(), get(), get(), get()) }
    viewModel { SelectGroupViewModel(get()) }
}
