package com.rovo.app.core.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rovo.app.core.network.NetworkCondition
import com.rovo.app.core.network.messageForEmptyState
import com.rovo.app.core.network.titleForEmptyState
import rovo.composeapp.generated.resources.Res
import rovo.composeapp.generated.resources.action_retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun RovoNetworkOfflineCard(
    condition: NetworkCondition,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    RovoSurfaceCard(modifier = modifier) {
        Text(
            text = condition.titleForEmptyState(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = condition.messageForEmptyState(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            RovoPrimaryButton(
                text = stringResource(Res.string.action_retry),
                onClick = onRetry,
            )
        }
    }
}
