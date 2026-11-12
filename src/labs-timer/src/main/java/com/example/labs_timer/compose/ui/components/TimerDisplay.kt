package com.example.labs_timer.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labs_timer.CountdownAction
import com.example.labs_timer.R
import com.example.labs_timer.Time
import com.example.labs_timer.compose.ui.theme.AndroidApplicationLaboratoriesTheme
import com.example.labs_timer.compose.ui.theme.Gray
import com.example.labs_timer.compose.ui.theme.LightBlack
import com.example.labs_timer.compose.ui.theme.White
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun TimerDisplay(
    time: Time,
    onAction: (CountdownAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(8.dp),
    ) {
        DigitColumnWrapper(
            value = time.minutes / 10,
            amount = 10.minutes,
            onAction = onAction,
            modifier = Modifier.weight(1f),
            currentFocusRequester = FocusRequester.Default,
        )
        DigitColumnWrapper(
            value = time.minutes % 10,
            amount = 1.minutes,
            onAction = onAction,
            modifier = Modifier.weight(1f),
            currentFocusRequester = FocusRequester.Default,
        )
        Text(
            text = stringResource(R.string.symbol_colon),
            style = TextStyle(
                color = White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        DigitColumnWrapper(
            value = time.seconds / 10,
            amount = 10.seconds,
            onAction = onAction,
            modifier = Modifier.weight(1f),
            currentFocusRequester = FocusRequester.Default,
        )
        DigitColumnWrapper(
            value = time.seconds % 10,
            amount = 1.seconds,
            onAction = onAction,
            modifier = Modifier.weight(1f),
            currentFocusRequester = FocusRequester.Default,
        )
    }
}

@Composable
@Preview
fun TimerDisplayPreview() {
    AndroidApplicationLaboratoriesTheme {
        TimerDisplay(
            time = Time(
                minutes = 12,
                seconds = 34,
            ),
            onAction = {},
        )
    }
}

@Composable
fun DigitColumnWrapper(
    value: Int,
    amount: Duration,
    onAction: (CountdownAction) -> Unit,
    modifier: Modifier = Modifier,
    currentFocusRequester: FocusRequester,
    nextFocusRequester: FocusRequester? = null,
) {
    DigitColumn(
        value = value,
        onValueIncrement = { onAction(CountdownAction.Increment(amount)) },
        onValueDecrement = { onAction(CountdownAction.Decrement(amount)) },
        onValueChanged = { }, // TODO onAction(CountdownAction.SetDigit(digit, it))
        label = "",
        modifier = modifier,
        currentFocusRequester = currentFocusRequester,
        nextFocusRequester = nextFocusRequester,
    )
}

@Composable
fun DigitColumn(
    value: Int,
    onValueIncrement: () -> Unit,
    onValueDecrement: () -> Unit,
    onValueChanged: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    currentFocusRequester: FocusRequester,
    nextFocusRequester: FocusRequester? = null,
) {
    val incrementButtonLabel = stringResource(R.string.adjust_increment, label)
    val decrementButtonLabel = stringResource(R.string.adjust_decrement, label)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        StyledIconButton(
            onClick = onValueIncrement,
            icon = painterResource(R.drawable.ic_add),
            label = incrementButtonLabel,
        )
        StyledDigitField(
            value = value,
            onValueChanged = onValueChanged,
            currentFocusRequester = currentFocusRequester,
            nextFocusRequester = nextFocusRequester,
        )
        StyledIconButton(
            onClick = onValueDecrement,
            icon = painterResource(R.drawable.ic_minus),
            label = decrementButtonLabel,
        )
    }
}

@Composable
@Preview
fun DigitColumnPreview() {
    AndroidApplicationLaboratoriesTheme {
        DigitColumn(
            value = 0,
            onValueIncrement = {},
            onValueDecrement = {},
            onValueChanged = {},
            label = "1 second",
            currentFocusRequester = FocusRequester.Default,
        )
    }
}

@Composable
fun StyledIconButton(
    onClick: () -> Unit,
    icon: Painter,
    label: String?,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
        )
    }
}

@Composable
@Preview
private fun StyledIconButtonPreview() {
    AndroidApplicationLaboratoriesTheme {
        StyledIconButton(
            onClick = {},
            icon = painterResource(R.drawable.ic_add),
            label = "Action",
        )
    }
}

@Composable
fun StyledDigitField(
    value: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    currentFocusRequester: FocusRequester,
    nextFocusRequester: FocusRequester? = null,
) {
    val shape = RoundedCornerShape(12.dp)

    DigitField(
        value = value,
        onValueChanged = onValueChanged,
        textStyle = TextStyle(
            color = White,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
        currentFocusRequester = currentFocusRequester,
        nextFocusRequester = nextFocusRequester,
        modifier = modifier
            .clip(shape)
            .background(LightBlack)
            .border(1.dp, Gray, shape)
            .padding(vertical = 16.dp, horizontal = 8.dp)
    )
}

@Composable
@Preview
fun StyledDigitFieldPreview() {
    AndroidApplicationLaboratoriesTheme {
        StyledDigitField(
            value = 0,
            onValueChanged = {},
            currentFocusRequester = FocusRequester.Default,
            nextFocusRequester = null,
        )
    }
}