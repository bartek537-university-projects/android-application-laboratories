package com.example.labs_timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class Countdown(private val source: TimeSource.WithComparableMarks = TimeSource.Monotonic) {

    var duration: Duration = MIN
        private set(value) {
            field = value.coerceIn(range = MIN..MAX)
        }

    var isActive: Boolean = false
        private set

    val isRunning: Boolean
        get() = start != null

    private var accumulated: Duration = Duration.ZERO
    private var start: TimeMark? = null

    private val segment: Duration
        get() = start?.elapsedNow() ?: Duration.ZERO

    private var offset: Duration = MIN

    val remaining: Duration
        get() {
            val rawRemaining = duration + offset - accumulated - segment

            if (rawRemaining <= MIN) {
                if (isRunning) {
                    pause()
                }
                return MIN
            }

            return rawRemaining.coerceAtMost(MAX)
        }

    operator fun plusAssign(amount: Duration) {
        if (isActive) {
            val currentRemaining = (duration + offset - accumulated - segment).coerceAtLeast(MIN)
            val targetRemaining = (currentRemaining + amount).coerceIn(MIN, MAX)
            val allowedAmount = targetRemaining - currentRemaining

            offset += allowedAmount
        } else {
            duration += amount
        }
    }

    operator fun minusAssign(amount: Duration) {
        plusAssign(-amount)
    }

    fun start() {
        if (start != null) {
            return
        }

        if (remaining <= MIN) {
            return
        }

        isActive = true
        start = source.markNow()
    }

    fun pause() {
        if (start == null) {
            return
        }

        accumulated += segment
        start = null
    }

    fun reset() {
        isActive = false
        accumulated = Duration.ZERO
        start = null
        offset = Duration.ZERO
    }

    companion object {
        private val MIN = 0.seconds
        private val MAX = 99.minutes + 59.seconds
    }
}

data class Time(
    val minutes: Int,
    val seconds: Int,
) {
    init {
        require(minutes in 0..99)
        require(seconds in 0..59)
    }

    companion object {
        val ZERO get() = Time(minutes = 0, seconds = 0)
    }
}

fun Duration.toTime(): Time {
    val duration = inWholeSeconds.toInt()

    val minutes = duration / 60
    val seconds = duration % 60

    return Time(minutes, seconds)
}

enum class CountdownState {
    RUNNING, PAUSED, STOPPED,
}

fun Countdown.getState() = when {
    isRunning -> CountdownState.RUNNING
    isActive -> CountdownState.PAUSED
    else -> CountdownState.STOPPED
}

data class CountdownUiState(
    val time: Time = Time.ZERO,
    val state: CountdownState = CountdownState.STOPPED,
)

sealed interface CountdownAction {
    data class Increment(val amount: Duration) : CountdownAction
    data class Decrement(val amount: Duration) : CountdownAction
    data object Start : CountdownAction
    data object Pause : CountdownAction
    data object Reset : CountdownAction
}

fun ticker(period: Duration) = flow {
    while (true) {
        emit(Unit)
        delay(period)
    }
}

class CountdownViewModel : ViewModel() {

    private val countdown = Countdown()

    private val _state = MutableStateFlow(CountdownUiState())
    val state = _state.asStateFlow()

    private var tickerJob: Job? = null

    fun submit(action: CountdownAction) = when (action) {
        is CountdownAction.Increment -> incrementCountdown(action.amount)
        is CountdownAction.Decrement -> decrementCountdown(action.amount)
        CountdownAction.Start -> startCountdown()
        CountdownAction.Pause -> pauseCountdown()
        CountdownAction.Reset -> resetCountdown()
    }

    private fun incrementCountdown(amount: Duration) {
        countdown += amount
        updateCountdownState()
    }

    private fun decrementCountdown(amount: Duration) {
        countdown -= amount
        updateCountdownState()
    }

    private fun startCountdown() {
        countdown.start()
        updateCountdownState()
        startTicker()
    }

    private fun pauseCountdown() {
        countdown.pause()

        updateCountdownState()
        stopTicker()
    }

    private fun resetCountdown() {
        countdown.reset()

        updateCountdownState()
        stopTicker()
    }

    private fun updateCountdownState() {
        _state.update {
            val time = countdown.remaining.toTime()
            val state = countdown.getState()

            it.copy(time = time, state = state)
        }
    }

    private fun startTicker() {
        if (tickerJob != null) {
            return
        }

        tickerJob = ticker(1.seconds) //
            .onEach {
                updateCountdownState()
            }
            .launchIn(viewModelScope)
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }
}
