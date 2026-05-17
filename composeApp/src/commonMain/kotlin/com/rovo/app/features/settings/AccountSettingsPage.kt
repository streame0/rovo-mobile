package com.rovo.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rovo.app.core.auth.AuthRepository
import com.rovo.app.core.auth.AuthState
import com.rovo.app.core.ui.RovoPrimaryButton
import com.rovo.app.core.ui.RovoStatusModal
import com.rovo.app.core.ui.RovoSurfaceCard
import kotlinx.coroutines.launch
import rovo.composeapp.generated.resources.Res
import rovo.composeapp.generated.resources.action_cancel
import rovo.composeapp.generated.resources.compose_settings_page_account
import rovo.composeapp.generated.resources.settings_account_email
import rovo.composeapp.generated.resources.settings_account_not_signed_in
import rovo.composeapp.generated.resources.settings_account_sign_out
import rovo.composeapp.generated.resources.settings_account_sign_out_confirm_message
import rovo.composeapp.generated.resources.settings_account_sign_out_confirm_title
import rovo.composeapp.generated.resources.settings_account_status
import rovo.composeapp.generated.resources.settings_account_status_anonymous
import rovo.composeapp.generated.resources.settings_account_status_signed_in
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.accountSettingsContent(
    isTablet: Boolean,
) {
    item {
        AccountSettingsBody(isTablet = isTablet)
    }
}

@Composable
private fun AccountSettingsBody(
    isTablet: Boolean,
) {
    val authState by AuthRepository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showSignOutConfirm by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RovoSurfaceCard {
            Text(
                text = stringResource(Res.string.compose_settings_page_account),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(14.dp))

            when (val state = authState) {
                is AuthState.Authenticated -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.settings_account_status),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = if (state.isAnonymous) {
                                stringResource(Res.string.settings_account_status_anonymous)
                            } else {
                                stringResource(Res.string.settings_account_status_signed_in)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (!state.isAnonymous && state.email != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(Res.string.settings_account_email),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = state.email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(Res.string.settings_account_not_signed_in),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        RovoPrimaryButton(
            text = stringResource(Res.string.settings_account_sign_out),
            onClick = { showSignOutConfirm = true },
        )
    }

    RovoStatusModal(
        title = stringResource(Res.string.settings_account_sign_out_confirm_title),
        message = stringResource(Res.string.settings_account_sign_out_confirm_message),
        isVisible = showSignOutConfirm,
        confirmText = stringResource(Res.string.settings_account_sign_out),
        dismissText = stringResource(Res.string.action_cancel),
        onConfirm = {
            showSignOutConfirm = false
            scope.launch { AuthRepository.signOut() }
        },
        onDismiss = { showSignOutConfirm = false },
    )
}
