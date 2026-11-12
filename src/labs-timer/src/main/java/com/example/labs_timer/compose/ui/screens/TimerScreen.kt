package com.example.labs_timer.compose.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.labs_timer.CountdownViewModel
import com.example.labs_timer.compose.ui.components.CountdownControls
import com.example.labs_timer.compose.ui.components.TimerDisplay
import com.example.labs_timer.compose.ui.theme.AndroidApplicationLaboratoriesTheme
import com.example.labs_timer.compose.ui.theme.Black

@Composable
fun TimerScreen(
    countdownViewModel: CountdownViewModel = viewModel()
) {

    val state by countdownViewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Black,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            TimerDisplay(
                time = state.time,
                onAction = countdownViewModel::submit,
                modifier = Modifier.weight(1f)
            )
            CountdownControls(
                state = state.state,
                onAction = countdownViewModel::submit,
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun TimerScreenPreview() {
    AndroidApplicationLaboratoriesTheme {
        TimerScreen()
    }
}