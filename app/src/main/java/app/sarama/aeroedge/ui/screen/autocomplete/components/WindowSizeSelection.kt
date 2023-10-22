package app.sarama.aeroedge.ui.screen.autocomplete.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.sarama.aeroedge.R
import app.sarama.aeroedge.service.autocomplete.AutoCompleteInputConfiguration
import app.sarama.aeroedge.ui.theme.AeroEdgeTheme
import app.sarama.aeroedge.ui.theme.Pink80
import app.sarama.aeroedge.ui.theme.Purple80

@Composable
fun WindowSizeSelection(
    inputConfiguration: AutoCompleteInputConfiguration,
    onWindowValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableIntStateOf(inputConfiguration.initialWordCount) }

    LaunchedEffect(key1 = Unit) {
        onWindowValueChange(inputConfiguration.initialWordCount)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.window_size_slider_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Slider(
                value = sliderValue.toFloat(),
                onValueChange = { value ->
                    sliderValue = value.toInt()

                    onWindowValueChange(sliderValue)
                },
                valueRange = inputConfiguration.minWordCount.toFloat()..inputConfiguration.maxWordCount.toFloat(),
                steps = 45,
                colors = SliderDefaults.colors(
                    activeTickColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Purple80,
                    inactiveTickColor = Pink80
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.window_size_wordcount).replace("{count}", sliderValue.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun PreviewWindowSizeSelection() {
    AeroEdgeTheme{
        WindowSizeSelection(
            inputConfiguration = AutoCompleteInputConfiguration(),
            onWindowValueChange = {}
        )
    }
}
