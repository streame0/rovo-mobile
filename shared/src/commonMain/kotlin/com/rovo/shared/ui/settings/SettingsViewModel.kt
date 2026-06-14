package com.rovo.shared.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rovo.shared.api.TraktApi
import com.rovo.shared.data.local.entity.ProfileEntity
import com.rovo.shared.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val traktApi: TraktApi
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileEntity?>(null)
    val profile = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            _profile.value = profileRepository.getDefaultProfile()
        }
    }

    fun updateProfile(update: (ProfileEntity) -> ProfileEntity) {
        val current = _profile.value ?: return
        val updated = update(current)
        _profile.value = updated
        viewModelScope.launch {
            profileRepository.saveProfile(updated)
        }
    }

    fun setTheme(themeId: String) {
        updateProfile { it.copy(themeId = themeId) }
    }

    fun getTraktAuthUrl(redirectUri: String): String {
        return traktApi.getAuthUrl(redirectUri)
    }

    fun handleTraktCode(code: String, redirectUri: String, clientSecret: String) {
        viewModelScope.launch {
            try {
                val response = traktApi.exchangeCodeForToken(code, redirectUri, clientSecret)
                updateProfile { it.copy(traktToken = response.access_token) }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
