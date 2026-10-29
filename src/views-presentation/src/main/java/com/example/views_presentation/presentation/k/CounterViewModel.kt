package com.example.views_presentation.presentation.k

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CounterViewModel(
    private val repository: CounterRepository = InMemoryCounterRepository()
) : ViewModel() {

    val count: StateFlow<Int> = repository.getValueFlow() //
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0,
        )

    fun increment() {
        repository.increment()
    }
}