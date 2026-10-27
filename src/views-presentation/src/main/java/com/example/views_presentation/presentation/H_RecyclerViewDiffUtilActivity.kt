package com.example.views_presentation.presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.databinding.ActivityHRecyclerViewDiffUtilBinding
import com.example.views_presentation.databinding.ItemHRecyclerDiffUtilBinding
import com.example.views_presentation.domain.Profile

@Suppress("ClassName")
class H_RecyclerViewDiffUtilActivity : ExampleActivity<ActivityHRecyclerViewDiffUtilBinding>() {

    override fun onCreateView(): ActivityHRecyclerViewDiffUtilBinding {
        return ActivityHRecyclerViewDiffUtilBinding.inflate(layoutInflater)
    }

    override fun onCreate() {
        val profiles = repository.getRandom(limit = 10).toMutableList()

        val adapter = HProfileListAdapter { profile ->
            display(profile.name)
        }
        adapter.submitList(profiles)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(this, RecyclerView.VERTICAL)
        )

        binding.addButton.setOnClickListener {
            val updatedList = adapter.currentList.toMutableList()
            val randomIndex = (0..adapter.currentList.size).random()

            val profile = repository.getRandom()
            updatedList.add(randomIndex, profile)

            adapter.submitList(updatedList)
        }
        binding.removeButton.setOnClickListener {
            val updatedList = adapter.currentList.toMutableList()
            val randomIndex = adapter.currentList.indices.random()

            val profile = updatedList.removeAt(randomIndex)

            adapter.submitList(updatedList)

            display("Removed ${profile.name}.")
        }
        binding.shuffleButton.setOnClickListener {
            val updatedList = adapter.currentList.toMutableList()

            updatedList.shuffle()
            adapter.submitList(updatedList)
        }
    }
}

private class HProfileListAdapter(
    private val listener: (Profile) -> Unit,
) : ListAdapter<Profile, HProfileListAdapter.ViewHolder>(ProfileDiffUtil()) {

    inner class ViewHolder(private val binding: ItemHRecyclerDiffUtilBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(profile: Profile, index: Int) = with(binding) {
            avatarImage.setImageResource(profile.avatar)
            nameText.text = "${profile.name} (index=$index)"
            root.setOnClickListener { listener(profile) }
        }
    }

    class ProfileDiffUtil : DiffUtil.ItemCallback<Profile>() {
        override fun areItemsTheSame(
            oldItem: Profile, newItem: Profile
        ): Boolean {
            // If profiles had identifiers, we would compare them instead.
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Profile, newItem: Profile
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemHRecyclerDiffUtilBinding //
            .inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, index: Int
    ) {
        val profile = getItem(index)
        holder.bind(profile, index)
    }
}
