package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared interface for PollViewModel so that UiUtils (shared module) can
 * cast the pollViewModel: Any? parameter without depending on feature:group module.
 */
interface PollViewModelInterface {
    val polls: StateFlow<Map<String, PollObject>>
    val myVotes: StateFlow<Map<String, List<Int>>>
    val submitState: StateFlow<Map<String, Boolean?>>
    val allVoters: StateFlow<Map<String, List<Pair<UserInstance?, List<Int>>>>>

    fun loadPoll(pollId: String, userId: String)
    fun refreshPoll(pollId: String, userId: String)
    fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>)
    fun resetSubmitState(pollId: String)
    fun clearMyVote(pollId: String)
    fun loadAllVoters(pollId: String)
    fun refreshAllVoters(pollId: String)
}

