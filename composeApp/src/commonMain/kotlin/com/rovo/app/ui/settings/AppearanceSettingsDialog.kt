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
fun AppearanceSettingsDialog(
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }

            item {
                AppearanceSection(title = "Theme") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemeOption(
                            name = "Dark",
                            isSelected = profile?.themeId != "light",
                            modifier = Modifier.weight(1f),
                            onClick = { onUpdate { it.copy(themeId = "dark") } }
                        )
                        ThemeOption(
                            name = "Light",
                            isSelected = profile?.themeId == "light",
                            modifier = Modifier.weight(1f),
                            onClick = { onUpdate { it.copy(themeId = "light") } }
                        )
                    }
                }
            }

            item {
                AppearanceSection(title = "UI Style") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppearanceToggle(
                            title = "Round Corners",
                            description = "Use rounded corners for posters and cards.",
                            checked = profile?.roundCorners ?: true,
                            onCheckedChange = { checked -> onUpdate { it.copy(roundCorners = checked) } }
                        )
                        AppearanceToggle(
                            title = "Hub Round Corners",
                            description = "Extra rounding for home screen sections.",
                            checked = profile?.hubRoundCorners ?: true,
                            onCheckedChange = { checked -> onUpdate { it.copy(hubRoundCorners = checked) } }
                        )
                    }
                }
            }

            item {
                AppearanceSection(title = "Layouts") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LayoutOption(
                            title = "Home Layout",
                            selected = profile?.homeTabLayout ?: "cinematic",
                            options = listOf("cinematic", "grid", "list"),
                            onSelect = { layout -> onUpdate { it.copy(homeTabLayout = layout) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.rovo.colors.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun ThemeOption(name: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.rovo.colors.primary.copy(alpha = 0.1f) else MaterialTheme.rovo.colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.rovo.colors.primary.copy(alpha = 0.5f) else MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
fun AppearanceToggle(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.rovo.colors.surface)
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.rovo.colors.textSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun LayoutOption(title: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSelected = selected == option
                Surface(
                    onClick = { onSelect(option) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.rovo.colors.primary else MaterialTheme.rovo.colors.surfaceVariant,
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            option.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.rovo.colors.onPrimary else MaterialTheme.rovo.colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
