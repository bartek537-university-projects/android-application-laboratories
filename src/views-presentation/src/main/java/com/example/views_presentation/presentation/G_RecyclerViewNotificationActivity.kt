package com.example.views_presentation.presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.views_presentation.ExampleActivity
import com.example.views_presentation.databinding.ActivityGRecyclerViewNotificationBinding
import com.example.views_presentation.databinding.ItemGRecyclerNotificationBinding
import com.example.views_presentation.domain.Profile

@Suppress("ClassName")
class G_RecyclerViewNotificationActivity :
    ExampleActivity<ActivityGRecyclerViewNotificationBinding>() {

    override fun onCreateView(): ActivityGRecyclerViewNotificationBinding {
        return ActivityGRecyclerViewNotificationBinding.inflate(layoutInflater)
    }

    override fun onCreate() {
        val profiles = repository.getRandom(limit = 10).toMutableList()

        val adapter = GProfileListAdapter(profiles) { profile ->
            display(profile.name)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(this, RecyclerView.VERTICAL)
        )

        binding.addButton.setOnClickListener {
            val randomIndex = (0..profiles.size).random()
            val profile = repository.getRandom()

            adapter.insert(profile, randomIndex)
        }
        binding.removeButton.setOnClickListener {
            val randomIndex = profiles.indices.random()

            val profile = adapter.remove(randomIndex)

            display("Removed ${profile.name}.")
        }
        binding.shuffleButton.setOnClickListener {
            adapter.shuffle()
        }
    }
}

private class GProfileListAdapter(
    private val profiles: MutableList<Profile>,
    private val listener: (Profile) -> Unit,
) : RecyclerView.Adapter<GProfileListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemGRecyclerNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(profile: Profile, index: Int) = with(binding) {
            avatarImage.setImageResource(profile.avatar)
            nameText.text = "${profile.name} (index=$index)"
            root.setOnClickListener { listener(profile) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemGRecyclerNotificationBinding //
            .inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, index: Int
    ) {
        val profile = profiles[index]
        holder.bind(profile, index)
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    fun insert(value: Profile, index: Int = profiles.size) {
        if (index !in 0..profiles.size) {
            throw IndexOutOfBoundsException("Invalid index $index, size is ${profiles.size}")
        }

        profiles.add(index, value)

        notifyItemInserted(index)
        notifyItemRangeChanged(index, profiles.size - index)
    }

    fun remove(index: Int = profiles.size): Profile {
        if (index !in 0..profiles.size) {
            throw IndexOutOfBoundsException("Invalid index $index, size is ${profiles.size}")
        }

        val profile = profiles.removeAt(index)

        notifyItemRemoved(index)
        notifyItemRangeChanged(index, profiles.size - index)

        return profile
    }

    @SuppressLint("NotifyDataSetChanged")
    fun shuffle() {
        profiles.shuffle()

        notifyDataSetChanged()
    }
}