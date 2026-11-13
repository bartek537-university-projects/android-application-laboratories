package com.example.labs_timer.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.labs_timer.CountdownAction
import com.example.labs_timer.CountdownState
import com.example.labs_timer.CountdownUiState
import com.example.labs_timer.CountdownViewModel
import com.example.labs_timer.databinding.ActivityTimerViewsBinding
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimerViewsActivity : AppCompatActivity() {

    private val viewModel by viewModels<CountdownViewModel>()

    private lateinit var binding: ActivityTimerViewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTimerViewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureEdgeToEdge()

        setupEventListeners()
        observeViewModelState()
    }

    fun configureEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupEventListeners() = with(binding) {
        incrementTensOfMinutesButton.setOnClickListener {
            viewModel.submit(CountdownAction.Increment(10.minutes))
        }
        incrementSingleMinutesButton.setOnClickListener {
            viewModel.submit(CountdownAction.Increment(1.minutes))
        }
        incrementTensOfSecondsButton.setOnClickListener {
            viewModel.submit(CountdownAction.Increment(10.seconds))
        }
        incrementSingleSecondsButton.setOnClickListener {
            viewModel.submit(CountdownAction.Increment(1.seconds))
        }
        decrementTensOfMinutesButton.setOnClickListener {
            viewModel.submit(CountdownAction.Decrement(10.minutes))
        }
        decrementSingleMinutesButton.setOnClickListener {
            viewModel.submit(CountdownAction.Decrement(1.minutes))
        }
        decrementTensOfSecondsButton.setOnClickListener {
            viewModel.submit(CountdownAction.Decrement(10.seconds))
        }
        decrementSingleSecondsButton.setOnClickListener {
            viewModel.submit(CountdownAction.Decrement(1.seconds))
        }
        startButton.setOnClickListener {
            viewModel.submit(CountdownAction.Start)
        }
        pauseButton.setOnClickListener {
            viewModel.submit(CountdownAction.Pause)
        }
        stopButton.setOnClickListener {
            viewModel.submit(CountdownAction.Reset)
        }
    }

    private fun observeViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    updateComponentsFromState(it)
                }
            }
        }
    }

    private fun updateComponentsFromState(state: CountdownUiState) = with(binding) {
        tensOfMinutesInput.editText?.setText("${state.time.minutes / 10}")
        singleMinutesInput.editText?.setText("${state.time.minutes % 10}")
        tensOfSecondsInput.editText?.setText("${state.time.seconds / 10}")
        singleSecondsInput.editText?.setText("${state.time.seconds % 10}")
        startButton.isEnabled = state.state != CountdownState.RUNNING
        pauseButton.isEnabled = state.state == CountdownState.RUNNING
        stopButton.isEnabled = state.state != CountdownState.STOPPED
    }
}