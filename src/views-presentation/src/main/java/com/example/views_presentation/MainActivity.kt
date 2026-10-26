package com.example.views_presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.views_presentation.presentation.A_NaiveApproachActivity
import com.example.views_presentation.presentation.B_SimpleListViewActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        startActivity<A_NaiveApproachActivity>()
        startActivity<B_SimpleListViewActivity>()
    }

    private inline fun <reified A> startActivity() {
        val intent = Intent(this, A::class.java)
        startActivity(intent)
    }
}