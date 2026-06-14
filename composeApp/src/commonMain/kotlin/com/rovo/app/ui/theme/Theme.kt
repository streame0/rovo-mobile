package com.rovo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun RovoTheme(
    darkTheme: Boolean = true, // Default to true for black theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val rovoTokens = RovoThemeTokens(
        colors = RovoColorTokens(
            background = colorScheme.background,
            surface = colorScheme.surface,
            surfaceVariant = colorScheme.surfaceVariant,
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            textPrimary = colorScheme.onSurface,
            textSecondary = colorScheme.onSurface.copy(alpha = RovoTokens.Opacity.secondary),
            textMuted = colorScheme.onSurface.copy(alpha = RovoTokens.Opacity.muted),
            accent = colorScheme.primary,
            error = colorScheme.error
        ),
        shapes = RovoShapeTokens(
            card = RoundedCornerShape(RovoTokens.Radius.lg),
            button = RoundedCornerShape(RovoTokens.Radius.full),
            poster = RoundedCornerShape(RovoTokens.Radius.md),
        )
    )

    CompositionLocalProvider(
        LocalRovoThemeTokens provides rovoTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}
