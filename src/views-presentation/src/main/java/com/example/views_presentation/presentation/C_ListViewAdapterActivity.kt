package com.example.views_presentation.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.R
import com.example.views_presentation.databinding.ActivityCListViewAdapterBinding
import com.example.views_presentation.databinding.ItemCListAdapterBinding
import com.example.views_presentation.domain.Profile

@Suppress("ClassName", "unused")
class C_ListViewAdapterActivity : ExampleActivity<ActivityCListViewAdapterBinding>() {

    override fun onCreateView(): ActivityCListViewAdapterBinding {
        return ActivityCListViewAdapterBinding.inflate(layoutInflater)
    }

    override fun onCreate() {
        val profiles = repository.getRandom(limit = 1_000) // Tested on 1_000, 20_000 and 1_000_000.

        val adapter = ProfileListAdapter(this, profiles)

        with(binding) {
            listView.onItemClickListener = { parent, view, position, id ->
                display(profiles[position].name)
            }
            listView.adapter = adapter
        }
    }
}

class ProfileListAdapter(private val context: Context, private val profiles: Array<Profile>) :
    ArrayAdapter<Profile>(context, resource, profiles) {

    companion object {
        private val resource = R.layout.item_c_list_adapter
    }

    override fun getView(
        position: Int, convertView: View?, parent: ViewGroup
    ): View {
        val inflater = LayoutInflater.from(context)
        val profile = profiles[position]

        val binding = if (convertView != null) {
            ItemCListAdapterBinding.bind(convertView)
        } else {
            ItemCListAdapterBinding.inflate(inflater, parent, false)
        }

        with(binding) {
            avatarImage.setImageResource(profile.avatar)
            nameText.text = profile.name
        }

        return binding.root
    }
}