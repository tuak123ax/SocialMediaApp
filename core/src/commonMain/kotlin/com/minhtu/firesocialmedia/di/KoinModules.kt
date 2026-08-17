package com.minhtu.firesocialmedia.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun appModule() = module {
    // ── Platform context as a singleton ────────────────────//
    single<PlatformContext> { PlatformContextHolder.instance }

    // ── Repositories (singletons) ────────────────────//

    // Note: CommonDbRepository has been decomposed by domain: liked-posts/news-offline-sync
    // is bound by feature/home's HomeModule, comment CRUD/likes by feature/comment's
    // CommentModule, friend requests by feature/friend's FriendModule, and login-activity
    // by feature/auth's SaveLoginActivityInfoUseCase (backed directly by feature/auth's own
    // AuthDatabaseService/IpInfoRemoteDataSource, not a repository) — all always loaded
    // alongside this module (see AppApplication.kt / KoinInitializer.kt).

    // Note: the old cross-cutting LocalRepository (feature/auth) was deleted in Phase 4;
    // its callers now inject narrow core interfaces (domain.usecases.local.*) implemented
    // by profile/home/notification/comment directly, or CryptoService where the operation
    // was crypto-backed rather than Room-backed.

    // Note: UserRepository/UserInstance were removed from core entirely — each feature now
    // owns its own local UserInstance clone + narrow UserRepository backed directly by
    // DatabaseService/UserRoomService (see each feature's own *Module.kt).

    // Note: ShowImageRepository is bound by feature/security's SecurityModule,
    // which is always loaded alongside this module (see AppApplication.kt / KoinInitializer.kt).
    // ShowImageViewModel and its DownloadImageUseCase factory also moved there (Phase 3).

    // Note: PollRepository was removed from core entirely — feature/home's NewsRepository
    // now declares the poll methods directly, and feature/group/feature/profile each own
    // their own poll passthroughs on their respective news repositories instead of sharing
    // feature/home's singleton. PostInformationViewModel moved to feature/home's HomeModule
    // (Phase 3).

    // Note: SettingsRepository was removed from core entirely — feature/security now owns
    // a trimmed interface (reAuthenticate/2FA/session-observing only). The other consumers
    // (feature/auth, feature/group, feature/profile) each inject the underlying
    // AuthService/DatabaseService, or feature/auth's own AuthDatabaseService/
    // IpInfoRemoteDataSource, directly instead of sharing a repository for what was a
    // single-method need.

    // Note: AuthSessionService (formerly PlatformContext.auth) was removed from core entirely —
    // each of its 8 consumers (feature/auth, friend, group, home, notification, profile,
    // security, appInit) now owns its own package-suffixed clone (interface + Android/iOS
    // Firebase-backed impl), bound in that module's own Koin graph, matching the same
    // "no shared cross-feature abstraction" convention as UserRepository/SettingsRepository above.
    // PlatformContext now only exposes networkMonitor.

    // Note: AuthException was likewise removed from core — it only had two consumers
    // (feature/auth, feature/security), each already using it with its own disjoint error-code
    // vocabulary, so each now owns its own package-suffixed clone of the same one-line class.

    // Note: ClipboardService was removed from core the same way — 4 consumers (feature/home,
    // security, group, comment), each a trivial one-method wrapper around the platform clipboard
    // with no shared state, so each now owns its own package-suffixed clone (interface +
    // Android/iOS impl), bound in that module's own Koin graph.

    // Note: AndroidCryptoHelper/IosCryptoHelper deliberately stay in core, unlike the types
    // above — they are not a false shared abstraction, they are genuinely shared *stateful*
    // infrastructure. Every consumer (feature/auth, home, profile, security, and core's own
    // TokenStorage) reads and writes the same underlying encrypted store (Android:
    // EncryptedSharedPreferences file "secure_prefs_file" under Keystore alias
    // "_androidx_security_master_key_"; iOS: the shared Keychain-backed `settings`) — e.g.
    // feature/auth writes KEY_FCM_TOKEN and feature/home reads it back; feature/home writes the
    // cached user info and feature/profile reads it back. Cloning this per feature would only
    // stay correct if every copy kept byte-for-byte identical file names/aliases/key constants
    // forever — an unenforced, silently-breakable coupling with no compiler safety net. So this
    // one keeps a single source of truth in core and features inject it directly instead.

    // Note: AndroidDatabaseHelper/IosDatabaseHelper/SupabaseStorage were removed from core the
    // same way as ClipboardService — every method opens a fresh Firebase reference or fires a
    // stateless Ktor HTTP call per invocation, no shared mutable data, so each consuming feature
    // (home, friend, security, group, comment, profile, auth) now owns its own package-suffixed,
    // narrowly-trimmed clone (only the methods that feature actually calls), bound/called
    // directly, no Koin binding needed since these were always plain object/companion-object
    // utilities rather than injected interfaces. `SupabaseClient`'s BASE_URL constant was folded
    // into each `SupabaseStorage` clone as a private const rather than kept as a separate file.
    //
    // `SupabaseStorageHelper` deliberately stays in core, unlike the types above — same reasoning
    // as AndroidCryptoHelper/IosCryptoHelper: it is genuinely shared *stateful* infrastructure, an
    // in-memory extension cache (Android: `ConcurrentHashMap`, iOS: `HashMap`) backed by a single
    // persisted store (Android: SharedPreferences file "supabase_ext_cache"; iOS: NSUserDefaults
    // key "supabase_ext_cache_v1"), warmed once at app startup and read/written by every feature
    // that resolves a media URL. Duplicating it per feature would give each clone its own empty
    // cache, defeating the point of caching and breaking the "warm once at startup" contract. So
    // it keeps a single source of truth in core, along with `SupabaseClient` (still needed by
    // `SupabaseStorageHelper` for its own base URL) and `KtorProvider`'s shared `HttpClient`,
    // which the per-feature `SupabaseStorage` clones call into rather than construct their own.

    // Note: ImagePicker and StorageProvider/SupabaseStorageProvider were removed from core
    // the same way as ClipboardService/AndroidDatabaseHelper — both are stateless (no shared
    // cache, no shared mutable resource; every method either builds a fresh platform picker
    // UI or does a pure string transform), so each consuming feature (ImagePicker: home, auth,
    // group, profile — home alone keeps pickVideo, the only consumer that calls it;
    // StorageProvider/SupabaseStorageProvider: appInit, home, friend, calling, security, auth,
    // group, notification, comment, profile) now owns its own package-suffixed clone (interface
    // + Android/iOS impl for ImagePicker; object + toStorageUrl() extension for
    // StorageProvider), called directly — no Koin binding needed for either, they were always
    // plain composable-factory-function / object-extension utilities rather than injected
    // interfaces.
    //
    // NetworkMonitor took a narrower cut: it stays in core, still backing
    // `PlatformContext.networkMonitor` for appInit's `Navigation.kt` global offline banner (a
    // genuine app-shell cross-cutting concern). But its other use — 5 features (home, group,
    // notification, comment, profile) injecting `get<PlatformContext>().networkMonitor` into
    // repository constructors purely for online/offline gating — was a false shared
    // abstraction, so those 5 each gained their own independent, package-suffixed
    // `NetworkMonitor` clone (interface + Android `ConnectivityManager`/iOS
    // `nw_path_monitor`-backed impl), bound via `single<NetworkMonitor> { ... }` in that
    // feature's own Koin module and injected in place of the old `PlatformContext` lookup. Two
    // parallel `NetworkMonitor` implementations now coexist by design — core's (app-shell
    // banner only) and each feature's own (repository gating only) — acceptable since the type
    // is stateless.

    // Note: createMessageForServer/createCallMessage/sendMessageToServer were removed from
    // core the same way as ClipboardService/AndroidDatabaseHelper — pure JSON-string builders
    // plus a fire-and-forget HTTP POST, no shared mutable state — so each of the 7 consuming
    // features (home, group, profile, comment, calling, security, notification) now owns its
    // own package-suffixed clone of only the functions it actually calls (see the table in
    // the migration plan), called directly, no Koin binding needed. Each clone's
    // sendMessageToServer still calls into core's existing `Client`/`NotificationApiService`
    // (Android) or `KtorProvider` (iOS) rather than cloning that plumbing, same as the
    // `SupabaseStorage` clones reuse core's `KtorProvider` — expensive-to-construct,
    // no-business-logic infra stays centralized even though the business logic around it
    // doesn't. iOS's createCallMessage was also fixed while cloning: it was previously an
    // unimplemented stub returning "", silently breaking call-signaling push (ring/accept/
    // reject) on iOS; each clone now builds the same JSON shape as the working Android
    // implementation via kotlinx.serialization's buildJsonObject.
    //
    // `TokenStorage` deliberately stays in core, unlike the functions above — same reasoning
    // as AndroidCryptoHelper/IosCryptoHelper: it writes into that same genuinely shared
    // encrypted store, and its only two consumers (Fire_Social_Media's
    // FirebaseNotificationService.kt/MainActivity.kt) are the Android app shell, not a
    // feature module, so there was nothing to clone it into.

    // Note: VideoPlayer was removed from core the same way as ImagePicker — stateless (each
    // call site already builds and owns its own ExoPlayer/AVPlayer instance, no shared pool
    // or cache), so each of its 4 consuming modules (appInit, home, group, profile) now owns
    // its own package-suffixed clone of the composable plus its private helper
    // composables/classes (Android: PlayerViewContent/FullscreenVideoDialog/
    // VideoActionButton.kt; iOS: InlineVideoPlayerView/IosFullscreenVideoOverlay/
    // IosActionButtonTokens and friends), called directly — no Koin binding needed. The
    // Android video disk cache (videoSimpleCache/getVideoCache) was dead code — zero call
    // sites, never wired to a player — so it was dropped entirely rather than carried into
    // any of the 4 clones.

    // ── Use Cases (factory = new instance each time) ────────────────────//

    // Note: DeletePollUseCase used to be a shared core use case bound to PollRepository;
    // it was removed in favor of each feature (home/group/profile) owning its own
    // DeletePollUseCase backed by its own news repository, matching the
    // "no shared cross-feature repository" architecture. SaveLikeNotificationUseCase
    // itself is cloned locally into feature/group and feature/profile (2
    // mutually-independent siblings, no Koin DI-contract needed for a concrete class)
    // — see each feature's own *Module.kt.

    // Note: ClearLocalDataUseCase (RoomService-based) moved fully to feature/security —
    // its only real consumer, AccountViewModel — see feature/security's SecurityModule.kt.

    // Note: CheckLocalAccountUseCase is bound by feature/auth's AuthModule, and
    // Get2FAVerifiedStatusUseCase by feature/security's SecurityModule — both always
    // loaded alongside this module (see AppApplication.kt / KoinInitializer.kt), and
    // both consumed by appInit's RouterViewModel.

    // Note: GetFCMTokenUseCase is bound by feature/auth's AuthModule — its only real
    // consumer is feature/auth's InformationViewModel (see AppApplication.kt / KoinInitializer.kt).

    // ── ViewModels ────────────────────//
    // Note: LoadingViewModel was split: each feature module now owns its own minimal
    // LoadingViewModel (isLoading/showLoading/hideLoading), bound in that feature's own
    // Koin module; the sync-loading piece moved to appInit's SyncLoadingViewModel, bound
    // in appInit's AppInitKoinModules.kt (same reasoning as RouterViewModel below).
    // Note: RouterViewModel moved to appInit's own Koin module (Phase 3) — it's a
    // composition-root/app-start-destination concern, not core infra.
}
