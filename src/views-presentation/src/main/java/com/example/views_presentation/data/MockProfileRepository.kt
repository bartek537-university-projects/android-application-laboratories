package com.example.views_presentation.data

import android.content.Context
import android.content.res.Resources
import androidx.core.content.res.getResourceIdOrThrow
import com.example.views_presentation.R
import com.example.views_presentation.domain.Profile
import com.example.views_presentation.domain.ProfileRepository

class MockProfileRepository(context: Context) : ProfileRepository {
    private val avatars: Array<Int>
    private val firstNames: Array<String>
    private val lastNames: Array<String>

    init {
        with(context.resources) {
            avatars = getResourceArray(R.array.avatars)
            firstNames = getStringArray(R.array.first_names)
            lastNames = getStringArray(R.array.last_names)
        }
    }

    override fun getRandom(limit: Int): Array<Profile> {
        return Array(limit) { getRandom() }
    }

    private fun getRandom(): Profile {
        val avatar = avatars.random()
        val name = "${firstNames.random()} ${lastNames.random()}"

        return Profile(avatar, name)
    }
}

private fun Resources.getResourceArray(id: Int): Array<Int> {
    return obtainTypedArray(id).use { array ->
        Array(size = array.length()) { index ->
            array.getResourceIdOrThrow(index)
        }
    }
}