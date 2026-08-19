package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.application.interactor.CallInteractorImpl
import com.minhtu.firesocialmedia.domain.interactor.CallInteractor
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.domain.usecases.call.AcceptCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.AddIceCandidatesUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.CreateOfferUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.EndCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ListenForIncomingCallsUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveAnswer
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveCallStatus
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveIceCandidateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObservePhoneCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObservePhoneCallWithInCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveVideoCall
import com.minhtu.firesocialmedia.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendAnswerUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendIceCandidateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendOfferUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendSignalingDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendWhoEndCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SetRemoteDescriptionUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StopCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StopObservePhoneCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateCameraStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateMicStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateSpeakerStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.database.CallDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.data.repository.CallRepositoryImpl
import com.minhtu.firesocialmedia.presentation.audiocall.CallViewModel
import com.minhtu.firesocialmedia.presentation.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.presentation.videocall.VideoCallViewModel
import com.minhtu.firesocialmedia.calling.presentation.loading.LoadingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun callingModule() = module {
    // -- Repository --
    single<CallRepository> {
        CallRepositoryImpl(
            { get<AudioCallService>() },
            get<CallDatabaseService>(),
            get<PermissionManager>()
        )
    }

    // -- Use Cases --
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

    factory { ListenForIncomingCallsUseCase(get()) }
    factory { ObservePhoneCallUseCase(get()) }
    factory { ObservePhoneCallWithInCallUseCase(get()) }
    factory { StopObservePhoneCallUseCase(get()) }
    factory { SetRemoteDescriptionUseCase(get()) }
    factory { SendAnswerUseCase(get()) }
    factory { AcceptCallUseCase(get()) }
    factory { AddIceCandidatesUseCase(get()) }
    factory { SendWhoEndCallUseCase(get()) }

    factory { StartCallUseCase(get(), get(), get()) }
    factory { SendOfferUseCase(get()) }
    factory { CreateOfferUseCase(get()) }
    factory { SendIceCandidateUseCase(get()) }
    factory { ObserveIceCandidateUseCase(get()) }
    factory { ObserveAnswer(get()) }
    factory { ObserveCallStatus(get()) }
    factory { ObserveVideoCall(get()) }
    factory { EndCallUseCase(get()) }

    // -- Interactor --
    factory<CallInteractor> { CallInteractorImpl(get(), get(), get()) }

    // -- ViewModels --
    viewModel { CallingViewModel(get(), get(), get()) }
    viewModel { VideoCallViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { LoadingViewModel() }

    single {
        CallViewModel(get())
    }

    // Note: CallingNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.
}
