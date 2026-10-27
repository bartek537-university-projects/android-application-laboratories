package com.example.views_presentation.presentation

import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.databinding.ActivityANaiveApproachBinding
import com.example.views_presentation.databinding.ItemANaiveApproachBinding

@Suppress("ClassName")
class A_NaiveApproachActivity : ExampleActivity<ActivityANaiveApproachBinding>() {

    override fun onCreateView(): ActivityANaiveApproachBinding {
        return ActivityANaiveApproachBinding.inflate(layoutInflater)
    }

    @Suppress("DestructuringDeclaration")
    override fun onCreate() {
        val profiles = repository.getRandom(limit = 1_000) // Tested on 1_000 and 20_000.

        for (profile in profiles) {
            val binding = ItemANaiveApproachBinding.inflate(layoutInflater)

            with(binding) {
                avatarImage.setImageResource(profile.avatar)
                nameText.text = profile.name

                root.setOnClickListener { display(profile.name) }
            }

            this@A_NaiveApproachActivity.binding.linearLayout.addView(binding.root)
        }
    }
}
