package com.example.views_presentation.domain

interface ProfileRepository {
    fun getRandom(limit: Int = 10): Array<Profile>
}