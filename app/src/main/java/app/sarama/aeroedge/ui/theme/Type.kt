package app.sarama.aeroedge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import app.sarama.aeroedge.R

// Set of Material typography styles to start with
val DemoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontSize = 20.sp,
        fontWeight = FontWeight.W800,
        letterSpacing = TextUnit(.05f, TextUnitType.Em),
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontSize = 14.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = TextUnit(.1f, TextUnitType.Em),
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontSize = 14.sp,
        fontWeight = FontWeight.W200,
        letterSpacing = TextUnit(.1f, TextUnitType.Em),
        lineHeight = 20.sp
    )
)