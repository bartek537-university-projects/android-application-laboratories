package com.example.views_presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.views_presentation.data.MockProfileRepository
import com.example.views_presentation.domain.ProfileRepository
import com.google.android.material.snackbar.Snackbar

abstract class ExampleActivity<Binding : ViewBinding> : AppCompatActivity() {

    protected lateinit var repository: ProfileRepository
    protected lateinit var binding: Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = MockProfileRepository(context = this)
        binding = onCreateView()

        setContentView(binding.root)

        onCreate()
    }

    protected abstract fun onCreateView(): Binding

    protected abstract fun onCreate()

    protected fun display(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
    }
}