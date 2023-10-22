package app.sarama.aeroedge.ui.screen.autocomplete.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.sarama.aeroedge.R
import app.sarama.aeroedge.ui.screen.autocomplete.TextEditBarState
import app.sarama.aeroedge.ui.theme.AeroEdgeTheme

@Composable
fun TextControlBar(
    state: TextEditBarState,
    onClearClick: () -> Unit,
    onGenerateClick: () -> Unit,
    onCopyClick: () -> Unit,
    onRetry: () -> Unit,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is TextEditBarState.Editing ->
            TextEditBar(
                onClearClick = onClearClick,
                onGenerateClick = onGenerateClick,
                onCopyClick = onCopyClick,
                barState = state,
                modifier = modifier
            )

        is TextEditBarState.Suggesting ->
            SuggestionControlBar(
                onRetry = onRetry,
                onAccept = onAccept,
                modifier = modifier
            )
    }
}

@Composable
fun TextEditBar(
    onClearClick: () -> Unit,
    onGenerateClick: () -> Unit,
    onCopyClick: () -> Unit,
    barState: TextEditBarState.Editing,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClearClick,
            enabled = barState.clearEnabled,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.RestartAlt,
                contentDescription = stringResource(R.string.clear_cta)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onGenerateClick,
            enabled = barState.generateEnabled,
            border = if (barState.generateEnabled) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .38f))
            },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier
                .height(40.dp)
                .width(180.dp)
        ) {
            if (barState.generating) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .scale(.5f)
                        .offset(0.dp, (-8).dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.generate_cta),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onCopyClick,
            enabled = barState.copyEnabled,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = stringResource(R.string.copy_cta)
            )
        }
    }
}

@Composable
fun SuggestionControlBar(
    onRetry: () -> Unit,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = onRetry,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.reject_suggestion_cta),
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onAccept,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(end = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    tint = MaterialTheme.colorScheme.tertiary,
                    contentDescription = null,
                    modifier = Modifier
                        .scale(.8f)
                        .padding(start = 24.dp)
                )
                Text(
                    text = stringResource(R.string.accept_suggestion_cta),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp)
                )
            }

        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun PreviewTextControlBar() {
    AeroEdgeTheme {
        Column {
            TextControlBar(
                state = TextEditBarState.Editing(
                    clearEnabled = false,
                    generateEnabled = true,
                    copyEnabled = false,
                    generating = false
                ),
                onClearClick = {},
                onGenerateClick = {},
                onCopyClick = {},
                onRetry = {},
                onAccept = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextControlBar(
                state = TextEditBarState.Suggesting,
                onClearClick = { },
                onGenerateClick = { },
                onCopyClick = {},
                onRetry = {},
                onAccept = {}
            )
        }
    }
}