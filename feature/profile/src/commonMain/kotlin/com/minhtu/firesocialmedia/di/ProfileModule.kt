package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.network.CheckInternetConnectionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserBackgroundUseCase
import com.minhtu.firesocialmedia.navigation.ProfileNavGraphImpl
import com.minhtu.firesocialmedia.presentation.personalinformation.PersonalInformationViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.navigation.ProfileNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun profileModule() = module {
    // Navigation graph
    single<ProfileNavGraph> { ProfileNavGraphImpl() }

    // Use cases unique to profile
    // (GetUserUseCase, GetCurrentUserUidUseCase are already provided by authModule)
    factory { SaveFriendUseCase(get()) }
    factory { SaveFriendRequestUseCase(get()) }
    factory { SaveNotificationToDatabaseUseCase(get()) }
    factory { CheckCalleeAvailableUseCase(get()) }
    factory { CheckInternetConnectionUseCase(get()) }
    factory { UpdateUserBackgroundUseCase(get()) }

    // ViewModels
    viewModel {
        UserInformationViewModel(
            get(), get(), get(), get(), get(), get(), get()
        )
    }
    viewModel {
        PersonalInformationViewModel(get(), get(), get(), get())
    }
    viewModel {
        InformationViewModel(get(), get(), get(), get())
    }
}
