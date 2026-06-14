package com.rovo.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object RovoGradients {
    val HeroOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.3f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.5f),
            Color.Black
        )
    )

    val GlassyOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.1f),
            Color.White.copy(alpha = 0.05f)
        )
    )
}
