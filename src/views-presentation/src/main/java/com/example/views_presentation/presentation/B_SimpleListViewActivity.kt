package com.example.views_presentation.presentation

import android.widget.ArrayAdapter
import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.R
import com.example.views_presentation.databinding.ActivityBSimpleListViewBinding

@Suppress("ClassName", "unused")
class B_SimpleListViewActivity : ExampleActivity<ActivityBSimpleListViewBinding>() {

    override fun onCreateView(): ActivityBSimpleListViewBinding {
        return ActivityBSimpleListViewBinding.inflate(layoutInflater)
    }

    override fun onCreate() {
        val profiles = repository.getRandom(limit = 1_000) // Tested on 1_000, 20_000 and 1_000_000.

        val adapter = ArrayAdapter(this, R.layout.item_b_text_view, profiles)

        with(binding) {
            listView.onItemClickListener = { parent, view, position, id ->
                display(profiles[position].name)
            }
            listView.adapter = adapter
        }
    }
}