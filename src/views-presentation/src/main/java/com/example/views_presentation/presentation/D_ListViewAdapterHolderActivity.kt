package com.example.views_presentation.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.R
import com.example.views_presentation.databinding.ActivityDListViewAdapterHolderBinding
import com.example.views_presentation.databinding.ItemDListHolderBinding
import com.example.views_presentation.domain.Profile

@Suppress("ClassName", "unused")
class D_ListViewAdapterHolderActivity : ExampleActivity<ActivityDListViewAdapterHolderBinding>() {

    override fun onCreateView(): ActivityDListViewAdapterHolderBinding {
        return ActivityDListViewAdapterHolderBinding.inflate(layoutInflater)
    }

    override fun onCreate() {
        val profiles = repository.getRandom(limit = 1_000) // Tested on 1_000, 20_000 and 1_000_000.

        val adapter = DProfileListAdapter(this, profiles)

        with(binding) {
            listView.onItemClickListener = { parent, view, position, id ->
                display(profiles[position].name)
            }
            listView.adapter = adapter
        }
    }
}

private class DProfileListAdapter(
    private val context: Context, private val profiles: Array<Profile>
) : ArrayAdapter<Profile>(context, resource, profiles) {

    private class ViewHolder(private val binding: ItemDListHolderBinding) {

        val view: View
            get() = binding.root

        fun bind(profile: Profile) = with(binding) {
            avatarImage.setImageResource(profile.avatar)
            nameText.text = profile.name
        }
    }

    companion object {
        private val resource = R.layout.item_c_list_adapter
    }

    override fun getView(
        position: Int, convertView: View?, parent: ViewGroup
    ): View {
        val inflater = LayoutInflater.from(context)
        val profile = profiles[position]

        val holder = if (convertView != null) {
            convertView.tag as ViewHolder
        } else {
            val binding = ItemDListHolderBinding.inflate(inflater, parent, false)
            ViewHolder(binding).apply { view.tag = this }
        }

        holder.bind(profile)

        return holder.view
    }
}