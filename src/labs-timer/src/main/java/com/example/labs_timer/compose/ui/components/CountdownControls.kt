package com.example.labs_timer.compose.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.labs_timer.CountdownAction
import com.example.labs_timer.CountdownState
import com.example.labs_timer.R
import com.example.labs_timer.compose.ui.theme.AndroidApplicationLaboratoriesTheme
import com.example.labs_timer.compose.ui.theme.Blue
import com.example.labs_timer.compose.ui.theme.Green
import com.example.labs_timer.compose.ui.theme.Red
import com.example.labs_timer.compose.ui.theme.White

@Composable
fun CountdownControls(
    state: CountdownState,
    onAction: (CountdownAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            onClick = { onAction(CountdownAction.Start) },
            text = stringResource(R.string.action_start),
            icon = painterResource(R.drawable.ic_play),
            enabled = state != CountdownState.RUNNING,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green,
                contentColor = White,
            ),
            modifier = Modifier.weight(1f),
        )
        ActionButton(
            onClick = { onAction(CountdownAction.Pause) },
            text = stringResource(R.string.action_pause),
            icon = painterResource(R.drawable.ic_pause),
            enabled = state == CountdownState.RUNNING,
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue,
                contentColor = White,
            ),
            modifier = Modifier.weight(1f),
        )
        ActionButton(
            onClick = { onAction(CountdownAction.Reset) },
            text = stringResource(R.string.action_stop),
            icon = painterResource(R.drawable.ic_stop),
            enabled = state != CountdownState.STOPPED,
            colors = ButtonDefaults.buttonColors(
                containerColor = Red,
                contentColor = White,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

class CountdownStateProvider : PreviewParameterProvider<CountdownState> {

    override val values: Sequence<CountdownState>
        get() = CountdownState.entries.asSequence()

    override fun getDisplayName(index: Int): String {
        val value = CountdownState.entries.getOrNull(index)
        return value?.toString() ?: "Unknown"
    }
}

@Composable
@Preview
fun CountdownControlsPreview(
    @PreviewParameter(CountdownStateProvider::class) state: CountdownState,
) {
    AndroidApplicationLaboratoriesTheme {
        CountdownControls(
            state = state,
            onAction = {},
        )
    }
}
