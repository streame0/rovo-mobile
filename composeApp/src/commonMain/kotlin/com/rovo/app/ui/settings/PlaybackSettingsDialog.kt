package com.rovo.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.data.local.entity.ProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsDialog(
    profile: ProfileEntity?,
    onDismiss: () -> Unit,
    onUpdate: ( (ProfileEntity) -> ProfileEntity ) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.rovo.colors.background,
        contentColor = MaterialTheme.rovo.colors.textPrimary,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.4f))
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Playback Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                PlaybackToggle(
                    title = "Autoplay Next Episode",
                    description = "Automatically start the next episode after finishing one.",
                    checked = profile?.autoplayNextEpisode ?: false,
                    onCheckedChange = { checked ->
                        onUpdate { it.copy(autoplayNextEpisode = checked) }
                    }
                )
            }

            item {
                PlaybackToggle(
                    title = "Frame Rate Matching",
                    description = "Adjust display refresh rate to match video content.",
                    checked = profile?.frameRateMatching ?: false,
                    onCheckedChange = { checked ->
                        onUpdate { it.copy(frameRateMatching = checked) }
                    }
                )
            }

            item {
                PlaybackToggle(
                    title = "Tunneling",
                    description = "Enable media tunneling for better performance on supported devices.",
                    checked = profile?.tunnelingEnabled ?: false,
                    onCheckedChange = { checked ->
                        onUpdate { it.copy(tunnelingEnabled = checked) }
                    }
                )
            }

            item {
                Text(
                    "Player Preference",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlayerOption(
                        name = "Internal",
                        isSelected = profile?.playerPreference == "internal",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdate { it.copy(playerPreference = "internal") } }
                    )
                    PlayerOption(
                        name = "External",
                        isSelected = profile?.playerPreference == "external",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdate { it.copy(playerPreference = "external") } }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaybackToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.rovo.colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (checked) MaterialTheme.rovo.colors.primary.copy(alpha = 0.3f) 
            else MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.rovo.colors.textSecondary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.rovo.colors.primary,
                    checkedTrackColor = MaterialTheme.rovo.colors.primary.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun PlayerOption(
    name: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.rovo.colors.primary.copy(alpha = 0.1f) else MaterialTheme.rovo.colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.rovo.colors.primary.copy(alpha = 0.5f) else MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.rovo.colors.primary else MaterialTheme.rovo.colors.textPrimary
            )
        }
    }
}
