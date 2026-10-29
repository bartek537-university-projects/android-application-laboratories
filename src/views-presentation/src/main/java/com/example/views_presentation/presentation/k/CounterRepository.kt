package com.example.views_presentation.presentation.k

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CounterRepository {
    fun getValueFlow(): Flow<Int>
    fun increment()
}

class InMemoryCounterRepository : CounterRepository {
    private val _count = MutableStateFlow(0)

    override fun getValueFlow(): Flow<Int> {
        return _count.asStateFlow()
    }

    override fun increment() {
        _count.value += 1
    }
}