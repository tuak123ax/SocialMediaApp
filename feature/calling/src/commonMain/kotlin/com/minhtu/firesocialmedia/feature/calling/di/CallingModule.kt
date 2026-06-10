package com.minhtu.firesocialmedia.feature.calling.di

import com.minhtu.firesocialmedia.core.application.interactor.CallInteractorImpl
import com.minhtu.firesocialmedia.core.domain.interactor.home.CallInteractor
import com.minhtu.firesocialmedia.core.domain.repository.CallRepository
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
import com.minhtu.firesocialmedia.data.repository.CallRepositoryImpl
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.feature.calling.navigation.CallingNavGraphImpl
import com.minhtu.firesocialmedia.feature.calling.presentation.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.feature.calling.presentation.videocall.VideoCallViewModel
import com.minhtu.firesocialmedia.presentation.navigation.CallingNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun callingModule() = module {
    // -- Repository --
    single<CallRepository> {
        CallRepositoryImpl(
            { get<PlatformContext>().audioCall },
            get<PlatformContext>().database,
            get<PlatformContext>().permissionManager
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

    // -- Navigation graph --
    single<CallingNavGraph> { CallingNavGraphImpl() }
}


