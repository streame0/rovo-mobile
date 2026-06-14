package com.rovo.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
fun AccountSettingsDialog(
    profile: ProfileEntity?,
    onDismiss: () -> Unit,
    onUpdate: ((ProfileEntity) -> ProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var selectedAvatar by remember { mutableStateOf(profile?.avatarRef ?: "avatar_1") }

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
                    .clip(CircleShape)
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
                "Profile Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar Selection
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.rovo.colors.primary,
                                    MaterialTheme.rovo.colors.primary.copy(alpha = 0.5f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.rovo.colors.onPrimary
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.rovo.colors.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Avatar",
                        modifier = Modifier.padding(6.dp).size(16.dp),
                        tint = MaterialTheme.rovo.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.rovo.colors.primary,
                    unfocusedBorderColor = MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.3f),
                    cursorColor = MaterialTheme.rovo.colors.primary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onUpdate { it.copy(name = name, avatarRef = selectedAvatar) }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.rovo.shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.rovo.colors.primary,
                    contentColor = MaterialTheme.rovo.colors.onPrimary
                )
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
