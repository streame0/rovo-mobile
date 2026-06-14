package com.rovo.app.ui.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.app.ui.theme.rovo

@Composable
fun SeasonSelector(
    seasons: List<Int>,
    selectedSeason: Int,
    onSeasonSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = RovoTokens.Space.s20),
        horizontalArrangement = Arrangement.spacedBy(RovoTokens.Space.s8)
    ) {
        items(seasons) { season ->
            val isSelected = season == selectedSeason
            Surface(
                onClick = { onSeasonSelect(season) },
                shape = RoundedCornerShape(RovoTokens.Radius.full),
                color = if (isSelected) MaterialTheme.rovo.colors.primary 
                        else MaterialTheme.rovo.colors.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = RovoTokens.Space.s20, vertical = RovoTokens.Space.s12),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Season $season",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) MaterialTheme.rovo.colors.onPrimary 
                                else MaterialTheme.rovo.colors.textPrimary,
                        letterSpacing = 0.5.sp,
                        fontSize = RovoTokens.Type.labelSm
                    )
                }
            }
        }
    }
}
