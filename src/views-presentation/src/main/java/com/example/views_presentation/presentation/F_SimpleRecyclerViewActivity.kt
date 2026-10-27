package com.example.views_presentation.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.databinding.ActivityFSimpleRecyclerViewBinding
import com.example.views_presentation.databinding.ItemEGridHolderBinding
import com.example.views_presentation.domain.Profile

@Suppress("ClassName")
class F_SimpleRecyclerViewActivity : ExampleActivity<ActivityFSimpleRecyclerViewBinding>() {

    override fun onCreateView(): ActivityFSimpleRecyclerViewBinding {
        return ActivityFSimpleRecyclerViewBinding.inflate(layoutInflater)
    }

    override fun onCreate() {
        val profiles = repository.getRandom(limit = 1_000)

        val adapter = FProfileListAdapter(profiles) { profile ->
            display(profile.name)
        }

        binding.recyclerView.adapter = adapter
//        binding.recyclerView.layoutManager = LinearLayoutManager(
//            this, RecyclerView.VERTICAL, false
//        )
        binding.recyclerView.layoutManager = GridLayoutManager(
            this, 3, RecyclerView.VERTICAL, false
        )
//        binding.recyclerView.addItemDecoration(
//            DividerItemDecoration(this, RecyclerView.VERTICAL)
//        )
    }
}

private class FProfileListAdapter(
    private val profiles: Array<Profile>,
    private val listener: (Profile) -> Unit,
) : RecyclerView.Adapter<FProfileListAdapter.ViewHolder>() {

    //    inner class ViewHolder(private val binding: ItemFRecyclerViewBinding) :
    inner class ViewHolder(private val binding: ItemEGridHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: Profile) = with(binding) {
            avatarImage.setImageResource(profile.avatar)
            nameText.text = profile.name
            root.setOnClickListener { listener(profile) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

//        val binding = ItemFRecyclerViewBinding //
//            .inflate(inflater, parent, false)
        val binding = ItemEGridHolderBinding //
            .inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int {
        return profiles.size
    }
}