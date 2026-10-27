package com.example.views_presentation.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.R
import com.example.views_presentation.databinding.ActivityEGridViewAdapterHolderBinding
import com.example.views_presentation.databinding.ItemEGridHolderBinding
import com.example.views_presentation.domain.Profile

@Suppress("ClassName", "unused")
class E_GridViewAdapterHolderActivity : ExampleActivity<ActivityEGridViewAdapterHolderBinding>() {

    override fun onCreateView(): ActivityEGridViewAdapterHolderBinding {
        return ActivityEGridViewAdapterHolderBinding.inflate(layoutInflater)
    }


    override fun onCreate() {
        val profiles = repository.getRandom(limit = 1000)  // Tested on 1_000, 20_000 and 1_000_000.

        val adapter = EProfileListAdapter(this, profiles)

        with(binding) {
            listView.onItemClickListener = { parent, view, position, id ->
                display(profiles[position].name)
            }
            listView.adapter = adapter
        }
    }
}

private class EProfileListAdapter(
    private val context: Context, private val profiles: Array<Profile>
) : ArrayAdapter<Profile>(context, resource, profiles) {

    private class ViewHolder(private val binding: ItemEGridHolderBinding) {

        val view: View
            get() = binding.root

        fun bind(profile: Profile) = with(binding) {
            avatarImage.setImageResource(profile.avatar)
            nameText.text = profile.name
        }
    }

    companion object {
        private val resource = R.layout.item_e_grid_holder
    }

    override fun getView(
        position: Int, convertView: View?, parent: ViewGroup
    ): View {
        val inflater = LayoutInflater.from(context)
        val profile = profiles[position]

        val holder = if (convertView != null) {
            convertView.tag as ViewHolder
        } else {
            val binding = ItemEGridHolderBinding.inflate(inflater, parent, false)
            ViewHolder(binding).apply { view.tag = this }
        }

        holder.bind(profile)

        return holder.view
    }
}