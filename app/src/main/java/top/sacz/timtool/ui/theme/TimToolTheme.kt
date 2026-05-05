package top.sacz.timtool.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TimToolDarkColorPalette.brandPrimaryColor,
    secondary = TimToolDarkColorPalette.brandPrimaryColor,
    tertiary = TimToolDarkColorPalette.accentTagColor,
    background = TimToolDarkColorPalette.dialogContainerColor,
    surface = TimToolDarkColorPalette.listItemCardColor,
    onPrimary = TimToolDarkColorPalette.textPrimaryColor,
    onSecondary = TimToolDarkColorPalette.textPrimaryColor,
    onTertiary = TimToolDarkColorPalette.statusOverlayTextColor,
    onBackground = TimToolDarkColorPalette.textPrimaryColor,
    onSurface = TimToolDarkColorPalette.textPrimaryColor
)

private val LightColorScheme = lightColorScheme(
    primary = TimToolLightColorPalette.brandPrimaryColor,
    secondary = TimToolLightColorPalette.brandPrimaryColor,
    tertiary = TimToolLightColorPalette.accentTagColor,
    background = TimToolLightColorPalette.dialogContainerColor,
    surface = TimToolLightColorPalette.listItemCardColor,
    onPrimary = TimToolLightColorPalette.textPrimaryColor,
    onSecondary = TimToolLightColorPalette.textPrimaryColor,
    onTertiary = TimToolLightColorPalette.statusOverlayTextColor,
    onBackground = TimToolLightColorPalette.textPrimaryColor,
    onSurface = TimToolLightColorPalette.textPrimaryColor
)

@Composable
fun TimToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val timToolPalette = if (darkTheme) TimToolDarkColorPalette else TimToolLightColorPalette
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(context)
        }

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> {
            dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalTimToolColorPalette provides timToolPalette) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                content = content
            )
        }
    }
}
