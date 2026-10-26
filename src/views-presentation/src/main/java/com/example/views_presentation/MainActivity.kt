package com.example.views_presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.views_presentation.presentation.A_NaiveApproachActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity<A_NaiveApproachActivity>()
    }

    private inline fun <reified A> startActivity() {
        val intent = Intent(this, A::class.java)
        startActivity(intent)
    }
}