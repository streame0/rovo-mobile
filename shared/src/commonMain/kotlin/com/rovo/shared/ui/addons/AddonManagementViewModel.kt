package com.rovo.shared.ui.addons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rovo.shared.data.local.entity.AddonEntity
import com.rovo.shared.repository.AddonRepository
import com.rovo.shared.data.local.dao.AddonDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddonManagementViewModel(
    private val repository: AddonRepository,
    private val dao: AddonDao
) : ViewModel() {

    val addons: StateFlow<List<AddonEntity>> = dao.getAllAddons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installState = MutableStateFlow<InstallState>(InstallState.Idle)
    val installState = _installState.asStateFlow()

    fun installAddon(url: String) {
        viewModelScope.launch {
            _installState.value = InstallState.Loading
            try {
                repository.installAddon(url)
                _installState.value = InstallState.Success
            } catch (e: Exception) {
                _installState.value = InstallState.Error(e.message ?: "Failed to install addon")
            }
        }
    }

    fun removeAddon(url: String) {
        viewModelScope.launch {
            dao.deleteAddonByUrl(url)
            dao.deleteCatalogConfigs(url)
        }
    }

    fun resetInstallState() {
        _installState.value = InstallState.Idle
    }
}

sealed interface InstallState {
    data object Idle : InstallState
    data object Loading : InstallState
    data object Success : InstallState
    data class Error(val message: String) : InstallState
}
