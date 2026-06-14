package com.rovo.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object RovoTokens {
    object Space {
        val none = 0.dp
        val s2 = 2.dp
        val s4 = 4.dp
        val s8 = 8.dp
        val s12 = 12.dp
        val s16 = 16.dp
        val s20 = 20.dp
        val s24 = 24.dp
        val s32 = 32.dp
    }

    object Radius {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 20.dp
        val xl = 24.dp
        val xxl = 32.dp
        val full = 999.dp
    }

    object Opacity {
        const val secondary = 0.70f
        const val muted = 0.60f
        const val strong = 0.85f
    }

    object Type {
        val labelSm = 12.sp
        val bodyMd = 14.sp
        val bodyLg = 16.sp
        val titleMd = 20.sp
        val titleLg = 24.sp
        val displaySm = 32.sp
        val displayMd = 40.sp
    }
}

@Immutable
data class RovoColorTokens(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val error: Color,
)

@Immutable
data class RovoShapeTokens(
    val card: Shape,
    val button: Shape,
    val poster: Shape,
)

@Immutable
data class RovoThemeTokens(
    val colors: RovoColorTokens,
    val shapes: RovoShapeTokens,
)

internal val LocalRovoThemeTokens = staticCompositionLocalOf {
    RovoThemeTokens(
        colors = RovoColorTokens(
            background = Color.Black,
            surface = Color(0xFF121212),
            surfaceVariant = Color(0xFF1A1A1A),
            primary = Color.White,
            onPrimary = Color.Black,
            textPrimary = Color.White,
            textSecondary = Color.White.copy(alpha = 0.7f),
            textMuted = Color.White.copy(alpha = 0.5f),
            accent = Color.White,
            error = Color(0xFFFF5252)
        ),
        shapes = RovoShapeTokens(
            card = RoundedCornerShape(RovoTokens.Radius.lg),
            button = RoundedCornerShape(RovoTokens.Radius.full),
            poster = RoundedCornerShape(RovoTokens.Radius.md),
        )
    )
}

val MaterialTheme.rovo: RovoThemeTokens
    @Composable
    @Stable
    get() = LocalRovoThemeTokens.current
