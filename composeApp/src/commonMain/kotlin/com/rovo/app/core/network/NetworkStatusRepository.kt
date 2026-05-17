package com.rovo.app.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkStatusUiState(
    val condition: NetworkCondition,
    val isOfflineLike: Boolean,
)

object NetworkStatusRepository {
    private val _uiState = MutableStateFlow(
        NetworkStatusUiState(
            condition = NetworkCondition.Unknown,
            isOfflineLike = false,
        )
    )

    val uiState: StateFlow<NetworkStatusUiState> = _uiState.asStateFlow()

    fun requestRefresh(force: Boolean) {
        _uiState.value = _uiState.value.copy(
            condition = NetworkCondition.Checking,
        )
    }

    fun ensureStarted() {
        _uiState.value = NetworkStatusUiState(
            condition = NetworkCondition.Online,
            isOfflineLike = false,
        )
    }

    fun updateAddonProbeTargets(targets: List<String>) {
        // no-op: addon probing removed with Supabase
    }
}
