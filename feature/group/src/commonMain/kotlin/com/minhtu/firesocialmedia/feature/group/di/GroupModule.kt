package com.minhtu.firesocialmedia.feature.group.di

import com.minhtu.firesocialmedia.feature.group.navigation.GroupNavGraphImpl
import com.minhtu.firesocialmedia.feature.group.presentation.creategroup.CreateGroupViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.createpoll.CreatePollViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.exploregroup.ExploreGroupViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.groupdetails.GroupDetailsViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.groupdetails.PollViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.invitemember.InviteMemberViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.managemembers.ManageMembersViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.selectgroup.SelectGroupViewModel
import com.minhtu.firesocialmedia.presentation.navigation.GroupNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun groupModule() = module {
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

