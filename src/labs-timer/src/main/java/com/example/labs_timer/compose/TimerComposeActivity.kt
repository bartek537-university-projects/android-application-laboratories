package com.example.labs_timer.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.labs_timer.compose.ui.screens.TimerScreen
import com.example.labs_timer.compose.ui.theme.AndroidApplicationLaboratoriesTheme

class TimerComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidApplicationLaboratoriesTheme {
                TimerScreen()
            }
        }
    }
}