package bd.ac.du.fm.noticetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DuBluePrimary,
    onPrimary = Color.White,
    secondary = DuBlueSecondary,
    onSecondary = Color.White,
    tertiary = DuRedAccent,
    background = DuBackground,
    surface = DuSurface,
    onSurface = TextPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = DuBlueSecondary,
    onPrimary = Color.Black,
    secondary = DuBluePrimary,
    onSecondary = Color.White,
    tertiary = DuRedAccent,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9)
)

@Composable
fun FmDuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
