package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.data.local.service.room.HomeUserRoomService
import com.minhtu.firesocialmedia.data.local.service.room.LikedPostRoomService
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService
import com.minhtu.firesocialmedia.data.repository.local.ClearCommentLocalDataUseCaseImpl
import com.minhtu.firesocialmedia.data.repository.local.ClearNewsLocalDataUseCaseImpl
import com.minhtu.firesocialmedia.data.repository.local.PreloadNotificationsUseCaseImpl
import com.minhtu.firesocialmedia.data.repository.local.StoreUserFriendsLocalUseCaseImpl
import com.minhtu.firesocialmedia.domain.usecases.home.FriendsLocalStore
import com.minhtu.firesocialmedia.entity.bridge.toAppInitUserDto
import com.minhtu.firesocialmedia.entity.bridge.toProfileUserDto
import com.minhtu.firesocialmedia.domain.usecases.home.NotificationPreloader
import com.minhtu.firesocialmedia.domain.usecases.home.security.LocalDataCleaner
import com.minhtu.firesocialmedia.navigation.AuthNavGraphImpl
import com.minhtu.firesocialmedia.navigation.CallingNavGraphImpl
import com.minhtu.firesocialmedia.navigation.GroupNavGraphImpl
import com.minhtu.firesocialmedia.navigation.FriendNavGraphImpl
import com.minhtu.firesocialmedia.navigation.HomeNavGraphImpl
import com.minhtu.firesocialmedia.navigation.NotificationNavGraphImpl
import com.minhtu.firesocialmedia.navigation.ProfileNavGraphImpl
import com.minhtu.firesocialmedia.navigation.SearchNavGraphImpl
import com.minhtu.firesocialmedia.navigation.SecurityNavGraphImpl
import com.minhtu.firesocialmedia.presentation.navigation.AuthNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.CallingNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.FriendNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.GroupNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.HomeNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.NotificationNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.ProfileNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.RouterViewModel
import com.minhtu.firesocialmedia.presentation.navigation.SearchNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.SecurityNavGraph
import com.minhtu.firesocialmedia.presentation.loading.SyncLoadingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun allFeatureModules(): List<Module> = listOf(
    authModule(),
    homeModule(),
    homeCommentModule(),
    profileModule(),
    commentModule(),
    groupModule(),
    securityModule(),
    notificationModule(),
    friendModule(),
    searchModule(),
    callingModule()
)

fun appInitModule() = module {
    viewModel { RouterViewModel(get(), get(), get(), get()) }

    // Sync loading indicator shown while syncing data after sign-in — moved here from
    // core's LoadingViewModel (which was split per-feature); this appInit-only piece
    // stays here since only Navigation.kt (composition root) uses it.
    viewModel { SyncLoadingViewModel() }

    // Home navigation graph — moved here from feature/home (Phase 3): it's a
    // composition-root/cross-feature nav contract, not feature-owned infra.
    single<HomeNavGraph> { HomeNavGraphImpl() }

    // Auth navigation graph — moved here from feature/auth (Phase 3), same reasoning.
    single<AuthNavGraph> { AuthNavGraphImpl() }

    // Profile navigation graph — moved here from feature/profile (Phase 3), same reasoning.
    single<ProfileNavGraph> { ProfileNavGraphImpl() }

    // Calling navigation graph — moved here from feature/calling (Phase 3), same reasoning.
    single<CallingNavGraph> { CallingNavGraphImpl() }

    // Group navigation graph — moved here from feature/group (Phase 3), same reasoning.
    single<GroupNavGraph> { GroupNavGraphImpl() }

    // Security navigation graph — moved here from feature/security (Phase 3), same reasoning.
    single<SecurityNavGraph> { SecurityNavGraphImpl() }

    // Notification navigation graph — moved here from feature/notification (Phase 3), same reasoning.
    single<NotificationNavGraph> { NotificationNavGraphImpl() }

    // Friend navigation graph — moved here from feature/friend (Phase 3), same reasoning.
    single<FriendNavGraph> { FriendNavGraphImpl() }

    // Search navigation graph — moved here from feature/search (Phase 3), same reasoning.
    single<SearchNavGraph> { SearchNavGraphImpl() }

    // Cross-feature use-case adapters: each interface below is owned by the *consuming*
    // feature (security/home); the concrete work is owned by a different feature
    // (comment/home/notification/profile) with no Gradle dependency on the consumer.
    // appInit depends on every feature, so it's the only place that can satisfy the
    // interface with the real implementation — same reasoning as the nav graphs above.
    single<List<LocalDataCleaner>> {
        listOf(
            LocalDataCleaner { get<ClearCommentLocalDataUseCaseImpl>().invoke() },
            LocalDataCleaner { get<ClearNewsLocalDataUseCaseImpl>().invoke() }
        )
    }
    single<NotificationPreloader> {
        val preloadNotificationsUseCaseImpl = get<PreloadNotificationsUseCaseImpl>()
        NotificationPreloader { preloadNotificationsUseCaseImpl() }
    }
    single<FriendsLocalStore> {
        val storeUserFriendsLocalUseCaseImpl = get<StoreUserFriendsLocalUseCaseImpl>()
        // Bridges feature/home's own UserDTO to feature/profile's own local UserDTO fork
        // (via core's UserDTO as the common intermediate shape).
        FriendsLocalStore { friends ->
            storeUserFriendsLocalUseCaseImpl(friends.map { it?.toAppInitUserDto()?.toProfileUserDto() })
        }
    }

    // feature/profile's LikedPostRoomService (its own local clone interface, no Gradle
    // dependency on feature/home) is bridged here to feature/home's HomeNewsRoomService, which
    // owns the actual NewsDao-backed LikedPost table — same underlying storage, no duplication.
    single<LikedPostRoomService> {
        val homeNewsRoomService = get<HomeNewsRoomService>()
        object : LikedPostRoomService {
            override suspend fun saveLikedPost(value: HashMap<String, Int>) =
                homeNewsRoomService.saveLikedPost(value)
            override suspend fun getAllLikedPosts(): HashMap<String, Int> =
                homeNewsRoomService.getAllLikedPosts()
            override suspend fun clearLikedPosts() = homeNewsRoomService.clearLikedPosts()
            override suspend fun hasLikedPost(): Boolean = homeNewsRoomService.hasLikedPost()
        }
    }

    // feature/home's HomeUserRoomService (its own local clone interface, no Gradle dependency
    // on feature/profile) is bridged here to feature/profile's UserRoomService, which owns the
    // actual UserDao-backed friends table.
    single<HomeUserRoomService> {
        val userRoomService = get<UserRoomService>()
        HomeUserRoomService { userRoomService.clearLocalFriends() }
    }
}
