package app.sarama.aeroedge.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import app.sarama.aeroedge.R
import app.sarama.aeroedge.ui.component.HeaderBar
import app.sarama.aeroedge.ui.screen.autocomplete.AutoCompleteScreen
import app.sarama.aeroedge.ui.theme.AeroEdgeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            val systemUiController = rememberSystemUiController()
            DisposableEffect(systemUiController) {
                // Update all of the system bar colors to be transparent, and use
                // dark icons if we're in light theme
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = true
                )
                systemUiController.setNavigationBarColor(Color.White)
                onDispose {}
            }

            AeroEdgeTheme {
                val insets = WindowInsets.statusBars.asPaddingValues()
                val barHeight = 66.dp

                Scaffold(
                    topBar = {
                        HeaderBar(
                            label = stringResource(R.string.header_autocomplete),
                            textOffset = (insets.calculateTopPadding() / 4),
                            modifier = Modifier.height(barHeight + insets.calculateTopPadding() / 2)
                        )
                    }
                ) { paddings ->
                    AutoCompleteScreen(
                        onShowToast = { id -> Toast.makeText(this, id, Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.padding(
                            top = barHeight - 20.dp,
                            bottom = paddings.calculateBottomPadding()
                        )
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewMain() {
    AeroEdgeTheme {
        Scaffold {
            AutoCompleteScreen(onShowToast = {}, modifier = Modifier.padding(top = 50.dp))
        }
    }
}