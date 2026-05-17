package com.rovo.app.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal expect val rovoPlatformExtraTopPadding: Dp
internal expect val rovoPlatformExtraBottomPadding: Dp
internal expect val rovoBottomNavigationExtraVerticalPadding: Dp
@Composable
internal expect fun rovoBottomNavigationBarInsets(): WindowInsets

internal val LocalRovoBottomNavigationOverlayPadding = staticCompositionLocalOf { 0.dp }

@Composable
internal fun rovoSafeBottomPadding(extra: Dp = 0.dp): Dp {
	val navigationBarBottom = rovoBottomNavigationBarInsets()
		.asPaddingValues()
		.calculateBottomPadding()
	return navigationBarBottom.coerceAtLeast(rovoPlatformExtraBottomPadding) +
		LocalRovoBottomNavigationOverlayPadding.current +
		extra
}
