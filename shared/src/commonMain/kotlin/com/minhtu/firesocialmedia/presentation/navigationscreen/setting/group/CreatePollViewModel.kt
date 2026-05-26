package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.domain.usecases.settings.CreatePollUseCase
import com.minhtu.firesocialmedia.platform.generateRandomId
import com.minhtu.firesocialmedia.platform.getCurrentTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatePollViewModel(
    private val createPollUseCase : CreatePollUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private fun generateRandomPollId() : String{
        return "Poll-" + generateRandomId()
    }

    private val _createPollState = MutableStateFlow<Boolean?>(null)
    val createPollState = _createPollState.asStateFlow()

    // Form state
    private val _question = MutableStateFlow("")
    val question = _question.asStateFlow()

    private val _options = MutableStateFlow(listOf("", ""))
    val options = _options.asStateFlow()

    private val _allowMultipleAnswers = MutableStateFlow(false)
    val allowMultipleAnswers = _allowMultipleAnswers.asStateFlow()

    private val _selectedDuration = MutableStateFlow("1 day")
    val selectedDuration = _selectedDuration.asStateFlow()

    private val _durationExpanded = MutableStateFlow(false)
    val durationExpanded = _durationExpanded.asStateFlow()

    fun setQuestion(value: String) { _question.value = value }
    fun setOption(index: Int, value: String) {
        _options.value = _options.value.toMutableList().also { it[index] = value }
    }
    fun addOption() {
        if (_options.value.size < 10) _options.value = _options.value + ""
    }
    fun removeOption(index: Int) {
        _options.value = _options.value.toMutableList().also { it.removeAt(index) }
    }
    fun setAllowMultipleAnswers(value: Boolean) { _allowMultipleAnswers.value = value }
    fun setSelectedDuration(value: String) { _selectedDuration.value = value }
    fun setDurationExpanded(value: Boolean) { _durationExpanded.value = value }
    fun createPoll(
        posterId : String,
        posterName : String,
        posterAvatar : String,
        question: String,
        options: List<String>,
        allowMultipleAnswers: Boolean,
        duration: String,
        groupId : String
    ) {
        viewModelScope.launch(ioDispatcher) {
            val pollId = generateRandomPollId()
            val newsId = pollId
            val now = getCurrentTime()
            val expiresAt = computeExpiresAt(now, duration)
            val pollObject = PollObject(
                id = pollId,
                posterId = posterId,
                posterName = posterName,
                posterAvatar = posterAvatar,
                question = question,
                options = options,
                allowMultipleAnswers = allowMultipleAnswers,
                duration = duration,
                groupId = groupId,
                timePosted = now,
                expiresAt = expiresAt
            )
            _createPollState.value = createPollUseCase.invoke(pollObject, newsId, groupId)
        }
    }

    /**
     * Parses a duration string like "1 day", "3 days", "1 week", "Never" and returns
     * the expiry epoch-millis. Returns null for "Never" or unrecognised strings (no expiry).
     */
    private fun computeExpiresAt(nowMillis: Long, duration: String): Long? {
        val lower = duration.lowercase().trim()
        val daysMs: Long = when {
            lower == "never" || lower.isEmpty() -> return null
            lower.contains("week") -> {
                val n = lower.filter { it.isDigit() }.toLongOrNull() ?: 1L
                n * 7 * 24 * 60 * 60 * 1000L
            }
            lower.contains("day") -> {
                val n = lower.filter { it.isDigit() }.toLongOrNull() ?: 1L
                n * 24 * 60 * 60 * 1000L
            }
            lower.contains("hour") -> {
                val n = lower.filter { it.isDigit() }.toLongOrNull() ?: 1L
                n * 60 * 60 * 1000L
            }
            else -> return null
        }
        return nowMillis + daysMs
    }

    fun resetCreatePollState() {
        _createPollState.value = null
    }

    fun resetCreatePollData() {
        _question.value = ""
        _options.value = listOf("", "")
        _allowMultipleAnswers.value = false
        _selectedDuration.value = "1 day"
        _durationExpanded.value = false
    }

}