package com.example.labs_timer.compose.ui.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun DigitField(
    value: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    currentFocusRequester: FocusRequester,
    nextFocusRequester: FocusRequester? = null,
) {
    require(value in 0..9)

    var text by remember(value) { mutableStateOf("$value") }

    fun cleanup(input: String): String {
        val digit = input.firstOrNull {
            it.isDigit()
        }
        return digit?.toString() ?: ""
    }

    BasicTextField(
        value = text,
        onValueChange = { input ->
            text = cleanup(input)
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
            imeAction = if (nextFocusRequester != null) {
                ImeAction.Next
            } else {
                ImeAction.Done
            },
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocusRequester?.requestFocus() },
            onDone = { onValueChanged(text.toIntOrNull() ?: 0) },
        ),
        singleLine = true,
        textStyle = textStyle,
        modifier = modifier
            .focusRequester(currentFocusRequester)
            .onFocusChanged { state ->
                if (!state.isFocused) {
                    return@onFocusChanged
                }
                onValueChanged(text.toIntOrNull() ?: 0)
            })
}