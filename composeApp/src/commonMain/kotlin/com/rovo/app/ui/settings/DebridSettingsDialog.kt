package com.rovo.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.data.local.entity.ProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebridSettingsDialog(
    profile: ProfileEntity?,
    onDismiss: () -> Unit,
    onUpdate: ( (ProfileEntity) -> ProfileEntity ) -> Unit
) {
    var realDebridToken by remember { mutableStateOf(profile?.realDebridToken ?: "") }
    var allDebridToken by remember { mutableStateOf(profile?.allDebridToken ?: "") }
    var selectedService by remember { mutableStateOf(profile?.debridService ?: "none") }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                "Debrid Services",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Speed up your streaming with a debrid service.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.rovo.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            DebridOption(
                name = "None",
                description = "Standard streaming experience",
                isSelected = selectedService == "none",
                onClick = { selectedService = "none" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            DebridOption(
                name = "Real-Debrid",
                description = "Unrestricted downloading and streaming",
                isSelected = selectedService == "realdebrid",
                onClick = { selectedService = "realdebrid" }
            )

            if (selectedService == "realdebrid") {
                TokenInputField(
                    value = realDebridToken,
                    onValueChange = { realDebridToken = it },
                    label = "Real-Debrid API Token"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            DebridOption(
                name = "AllDebrid",
                description = "Fast and reliable debrid service",
                isSelected = selectedService == "alldebrid",
                onClick = { selectedService = "alldebrid" }
            )

            if (selectedService == "alldebrid") {
                TokenInputField(
                    value = allDebridToken,
                    onValueChange = { allDebridToken = it },
                    label = "AllDebrid API Token"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onUpdate { 
                        it.copy(
                            debridService = selectedService,
                            realDebridToken = if (selectedService == "realdebrid") realDebridToken else it.realDebridToken,
                            allDebridToken = if (selectedService == "alldebrid") allDebridToken else it.allDebridToken
                        )
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.rovo.shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.rovo.colors.primary,
                    contentColor = MaterialTheme.rovo.colors.onPrimary
                )
            ) {
                Text("Save Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DebridOption(
    name: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.rovo.colors.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.rovo.colors.surface
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.rovo.colors.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.rovo.colors.textPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.rovo.colors.textSecondary
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.rovo.colors.primary
                )
            }
        }
    }
}

@Composable
fun TokenInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Column(modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.rovo.colors.primary,
                unfocusedBorderColor = MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.rovo.colors.primary
            )
        )
    }
}
