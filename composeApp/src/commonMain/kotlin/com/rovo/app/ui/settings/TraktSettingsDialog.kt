package com.rovo.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.data.local.entity.ProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraktSettingsDialog(
    profile: ProfileEntity?,
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                "Trakt.tv",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Sync your watch history, progress, and watchlist across all your devices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.rovo.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            val isLoggedIn = !profile?.traktToken.isNullOrEmpty()

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.rovo.colors.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.rovo.colors.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.rovo.colors.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isLoggedIn) "Account Connected" else "Not Connected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.rovo.colors.textPrimary
                    )
                    
                    Text(
                        text = if (isLoggedIn) "Your watch history is being synced." else "Sign in to enable synchronization.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.rovo.colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoggedIn) {
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.rovo.shapes.button,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.rovo.colors.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.rovo.colors.error.copy(alpha = 0.3f))
                        ) {
                            Text("Disconnect Trakt", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onLogin,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.rovo.shapes.button,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.rovo.colors.primary,
                                contentColor = MaterialTheme.rovo.colors.onPrimary
                            )
                        ) {
                            Text("Sign in with Trakt", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
