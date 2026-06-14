package com.rovo.app.ui.details.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.app.ui.theme.rovo

@Composable
fun DetailActionRow(
    onPlayClick: () -> Unit,
    onMoreClick: () -> Unit,
    onEpisodesClick: () -> Unit,
    playLabel: String,
    isLoadingStreams: Boolean,
    isSeries: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RovoTokens.Space.s20),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPlayClick,
            enabled = !isLoadingStreams,
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.rovo.colors.primary,
                contentColor = MaterialTheme.rovo.colors.onPrimary,
                disabledContainerColor = MaterialTheme.rovo.colors.primary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(RovoTokens.Radius.lg),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            if (isLoadingStreams) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.rovo.colors.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    playLabel.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
        
        Surface(
            shape = RoundedCornerShape(RovoTokens.Radius.lg),
            color = MaterialTheme.rovo.colors.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(60.dp),
            onClick = onMoreClick,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "More Actions",
                    tint = MaterialTheme.rovo.colors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (isSeries) {
            Surface(
                shape = RoundedCornerShape(RovoTokens.Radius.lg),
                color = MaterialTheme.rovo.colors.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .weight(0.6f)
                    .height(60.dp),
                onClick = onEpisodesClick,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "EPISODES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.rovo.colors.textPrimary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
