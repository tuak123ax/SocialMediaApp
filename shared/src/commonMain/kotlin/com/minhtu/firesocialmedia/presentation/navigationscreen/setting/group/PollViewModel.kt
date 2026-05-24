package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.FetchPollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.LoadAllVotersUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.LoadMyVotesUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SubmitVoteUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Holds the per-poll UI state: full poll data, current user's votes, and submission state.
 * Designed to be a shared ViewModel scoped to the group screen so multiple PollCards
 * in the same feed share one ViewModel (keyed by pollId internally).
 */
class PollViewModel(
    private val fetchPollUseCase: FetchPollUseCase,
    private val loadMyVotesUseCase: LoadMyVotesUseCase,
    private val submitVoteUseCase: SubmitVoteUseCase,
    private val loadAllVotersUseCase: LoadAllVotersUseCase? = null,
    private val getUserUseCase: GetUserUseCase? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // Cache of fully-loaded polls: pollId -> PollObject
    private val _polls = MutableStateFlow<Map<String, PollObject>>(emptyMap())
    val polls: StateFlow<Map<String, PollObject>> = _polls.asStateFlow()

    // Cache of current user votes: pollId -> selected option indices
    private val _myVotes = MutableStateFlow<Map<String, List<Int>>>(emptyMap())
    val myVotes: StateFlow<Map<String, List<Int>>> = _myVotes.asStateFlow()

    // Tracks what is actually persisted on the server for each poll.
    // Used as previousIndices when submitting a changed vote.
    // Differs from _myVotes only while the user is in "change vote" mode.
    private val _serverVotes = mutableMapOf<String, List<Int>>()

    // pollIds currently loading (to avoid duplicate fetches)
    private val loadingPollIds = mutableSetOf<String>()

    // submitVote result: pollId -> success flag  (null = idle)
    private val _submitState = MutableStateFlow<Map<String, Boolean?>>(emptyMap())
    val submitState: StateFlow<Map<String, Boolean?>> = _submitState.asStateFlow()

    // All voters for a poll (poll owner only): pollId -> list of (user, selectedIndices)
    private val _allVoters = MutableStateFlow<Map<String, List<Pair<UserInstance?, List<Int>>>>>(emptyMap())
    val allVoters: StateFlow<Map<String, List<Pair<UserInstance?, List<Int>>>>> = _allVoters.asStateFlow()

    // Track which polls have had voters loaded
    private val loadedVoterPollIds = mutableSetOf<String>()

    /**
     * Load full poll data + current user's votes.
     * Safe to call multiple times — skips if already loaded/loading.
     */
    fun loadPoll(pollId: String, userId: String) {
        if (pollId.isEmpty()) return
        if (_polls.value.containsKey(pollId) && _myVotes.value.containsKey(pollId)) return
        if (loadingPollIds.contains(pollId)) return
        loadingPollIds.add(pollId)

        viewModelScope.launch(ioDispatcher) {
            val poll = fetchPollUseCase(pollId)
            val votes = loadMyVotesUseCase(pollId, userId)

            if (poll != null) {
                _polls.value = _polls.value + (pollId to poll)
            }
            _myVotes.value = _myVotes.value + (pollId to votes)
            _serverVotes[pollId] = votes          // keep server-truth in sync
            loadingPollIds.remove(pollId)
        }
    }

    /**
     * Force-refresh a poll (e.g. after another user votes and we want live results).
     * Keeps existing poll data in cache so UI doesn't flash "Loading poll…" during refresh.
     */
    fun refreshPoll(pollId: String, userId: String) {
        if (loadingPollIds.contains(pollId)) return
        loadingPollIds.add(pollId)
        viewModelScope.launch(ioDispatcher) {
            val poll = fetchPollUseCase(pollId)
            val votes = loadMyVotesUseCase(pollId, userId)
            if (poll != null) {
                _polls.value = _polls.value + (pollId to poll)
            }
            _myVotes.value = _myVotes.value + (pollId to votes)
            _serverVotes[pollId] = votes          // keep server-truth in sync
            loadingPollIds.remove(pollId)
        }
    }

    fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>
    ) {
        viewModelScope.launch(ioDispatcher) {
            // Always use the server-persisted votes as the previous state for decrement/increment,
            // NOT _myVotes which may have been cleared by clearMyVote().
            val previous = _serverVotes[pollId] ?: emptyList()
            val success = submitVoteUseCase(pollId, userId, selectedIndices, previous)
            if (success) {
                // Update both caches to reflect the newly submitted vote.
                _serverVotes[pollId] = selectedIndices
                _myVotes.value = _myVotes.value + (pollId to selectedIndices)
                // Refresh poll to get updated server vote counts.
                refreshPoll(pollId, userId)
            }
            _submitState.value = _submitState.value + (pollId to success)
        }
    }

    fun resetSubmitState(pollId: String) {
        _submitState.value = _submitState.value + (pollId to null)
    }

    /**
     * Clears only the display vote cache so the user sees selectable options again.
     * _serverVotes is intentionally NOT cleared — it still holds the real persisted indices
     * so that a subsequent submitVote can correctly decrement the old option counts.
     */
    fun clearMyVote(pollId: String) {
        _myVotes.value = _myVotes.value + (pollId to emptyList())
    }

    /**
     * Load all voters for a poll. Only intended for the poll owner.
     * Fetches userId -> indices map and resolves user names via [getUserUseCase].
     */
    fun loadAllVoters(pollId: String) {
        if (pollId.isEmpty()) return
        if (loadedVoterPollIds.contains(pollId)) return
        loadedVoterPollIds.add(pollId)

        val votersUseCase = loadAllVotersUseCase ?: return
        viewModelScope.launch(ioDispatcher) {
            val voterMap = votersUseCase(pollId)
            val resolved = voterMap.map { (uid, indices) ->
                val user = getUserUseCase?.invoke(uid, false)
                Pair(user, indices)
            }
            _allVoters.value = _allVoters.value + (pollId to resolved)
        }
    }

    /** Refresh the voter list (e.g. after a new vote is submitted). */
    fun refreshAllVoters(pollId: String) {
        loadedVoterPollIds.remove(pollId)
        loadAllVoters(pollId)
    }
}
