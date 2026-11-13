package com.example.labs_timer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.labs_timer.compose.TimerComposeActivity
import com.example.labs_timer.views.TimerViewsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity<TimerViewsActivity>()
//        startActivity<TimerComposeActivity>()
    }

    private inline fun <reified A : Activity> startActivity() {
        val intent = Intent(this, A::class.java)
        startActivity(intent)
    }
}