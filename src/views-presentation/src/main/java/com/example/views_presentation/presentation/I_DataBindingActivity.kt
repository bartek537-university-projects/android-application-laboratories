package com.example.views_presentation.presentation

import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.databinding.ActivityIDataBindingBinding

@Suppress("ClassName")
class I_DataBindingActivity : ExampleActivity<ActivityIDataBindingBinding>() {

    override fun onCreateView(): ActivityIDataBindingBinding {
        return ActivityIDataBindingBinding.inflate(layoutInflater)
    }

    override fun onCreate() {
        binding.activity = this
        setRandomProfile()
    }

    fun setRandomProfile() {
        binding.profile = repository.getRandom()
    }
}