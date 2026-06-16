package com.example.tarik.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val LightColors = lightColorScheme(
    primary = TarikRed,
    onPrimary = TarikOnRed,
    primaryContainer = TarikRed,
    onPrimaryContainer = TarikOnRed,
    background = TarikCream,
    surface = TarikSurface,
    onBackground = TarikCharcoal,
    onSurface = TarikCharcoal,
    onSurfaceVariant = TarikMuted
)

private val DarkColors = darkColorScheme(
    primary = TarikRedDark,
    onPrimary = TarikOnRed,
    primaryContainer = TarikRedDark,
    onPrimaryContainer = TarikOnRed,
    background = TarikCharcoal,
    surface = TarikSurfaceDark,
    onBackground = TarikCream,
    onSurface = TarikCream,
    onSurfaceVariant = TarikMutedDark
)


@Composable
fun TarikTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}