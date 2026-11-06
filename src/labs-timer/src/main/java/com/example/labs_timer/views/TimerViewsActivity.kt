package com.example.labs_timer.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.labs_timer.R
import com.example.labs_timer.databinding.ActivityTimerViewsBinding

class TimerViewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerViewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTimerViewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureEdgeToEdge()
    }

    fun configureEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}