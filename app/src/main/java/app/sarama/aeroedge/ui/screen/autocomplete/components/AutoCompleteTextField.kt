package app.sarama.aeroedge.ui.screen.autocomplete.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import app.sarama.aeroedge.R
import app.sarama.aeroedge.ui.theme.ActiveOutlinedTextFieldBackground
import app.sarama.aeroedge.ui.theme.InactiveOutlinedTextFieldBackground
import app.sarama.aeroedge.ui.theme.InactiveOutlinedTextFieldBorder
import app.sarama.aeroedge.ui.theme.Purple40
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    inputValue: TextFieldValue,
    inputEnabled: Boolean,
    onInputValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    SideEffect {
        if (inputEnabled) {
            scope.launch {
                focusRequester.requestFocus()
            }
        }
    }

    OutlinedTextField(
        value = inputValue,
        onValueChange = onInputValueChange,
        enabled = inputEnabled,
        textStyle = MaterialTheme.typography.bodySmall,
        shape = MaterialTheme.shapes.medium,
        placeholder = {
            Text(
                text = stringResource(R.string.input_hint),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.alpha(.7f)
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = .7f),
            disabledBorderColor = InactiveOutlinedTextFieldBorder,
            focusedBorderColor = Purple40,
            containerColor = when {
                inputValue.text.isEmpty() -> InactiveOutlinedTextFieldBackground
                inputEnabled -> ActiveOutlinedTextFieldBackground
                else -> InactiveOutlinedTextFieldBackground
            }
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
        modifier = modifier
            .focusRequester(focusRequester)
    )
}