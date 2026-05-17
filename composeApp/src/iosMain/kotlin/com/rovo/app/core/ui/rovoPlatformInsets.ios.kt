package com.rovo.app.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal actual val rovoPlatformExtraTopPadding: Dp = 0.dp
internal actual val rovoPlatformExtraBottomPadding: Dp = 0.dp
internal actual val rovoBottomNavigationExtraVerticalPadding: Dp = 0.dp

@Composable
internal actual fun rovoBottomNavigationBarInsets(): WindowInsets =
	WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
