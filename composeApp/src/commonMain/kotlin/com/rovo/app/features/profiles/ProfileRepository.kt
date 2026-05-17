package com.rovo.app.features.profiles

import co.touchlab.kermit.Logger
import com.rovo.app.core.auth.AuthRepository
import com.rovo.app.core.auth.AuthState
import com.rovo.app.features.addons.AddonRepository
import com.rovo.app.features.collection.CollectionMobileSettingsRepository
import com.rovo.app.features.collection.CollectionRepository
import com.rovo.app.features.downloads.DownloadsRepository
import com.rovo.app.features.details.MetaScreenSettingsRepository
import com.rovo.app.features.home.HomeCatalogSettingsRepository
import com.rovo.app.features.home.HomeRepository
import com.rovo.app.core.ui.PosterCardStyleRepository
import com.rovo.app.features.library.LibraryRepository
import com.rovo.app.features.mdblist.MdbListSettingsRepository
import com.rovo.app.features.notifications.EpisodeReleaseNotificationsRepository
import com.rovo.app.features.player.PlayerSettingsRepository
import com.rovo.app.features.plugins.PluginRepository
import com.rovo.app.features.search.SearchHistoryRepository
import com.rovo.app.features.settings.ThemeSettingsRepository
import com.rovo.app.features.trakt.TraktAuthRepository
import com.rovo.app.features.trakt.TraktSettingsRepository
import com.rovo.app.features.tmdb.TmdbSettingsRepository
import com.rovo.app.features.watched.WatchedRepository
import com.rovo.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.rovo.app.features.watchprogress.WatchProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import rovo.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Serializable
private data class StoredProfilePayload(
    val userId: String,
    val activeProfileIndex: Int = 1,
    val profiles: List<RovoProfile> = emptyList(),
)

object ProfileRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("ProfileRepository")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private fun localizedString(resource: StringResource): String = runBlocking { getString(resource) }

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private var activeProfileIndex: Int = 1
    private var loadedCacheForUserId: String? = null

    val activeProfileId: Int get() = activeProfileIndex

    fun loadCachedProfiles(): Boolean {
        val stored = decodeStoredPayload() ?: return false
        loadedCacheForUserId = stored.userId
        applyStoredPayload(stored)
        ThemeSettingsRepository.onProfileChanged()
        return _state.value.profiles.isNotEmpty()
    }

    fun ensureLoaded(userId: String) {
        if (loadedCacheForUserId == userId && _state.value.isLoaded) return

        val stored = decodeStoredPayload()
        loadedCacheForUserId = userId
        if (stored == null) {
            _state.value = ProfileState()
            activeProfileIndex = 1
            return
        }

        if (stored.userId != userId) {
            _state.value = ProfileState()
            activeProfileIndex = 1
            return
        }

        applyStoredPayload(stored)
    }

    fun clearInMemory() {
        loadedCacheForUserId = null
        activeProfileIndex = 1
        _state.value = ProfileState()
    }

    fun onProfilesLoaded() {
        if (!_state.value.isLoaded) {
            _state.value = _state.value.copy(isLoaded = true)
        }
    }

    fun selectProfile(profileIndex: Int) {
        activeProfileIndex = profileIndex
        _state.value = _state.value.copy(
            activeProfile = _state.value.profiles.find { it.profileIndex == profileIndex },
        )
        persist()
        WatchedRepository.onProfileChanged(profileIndex)
        TraktSettingsRepository.onProfileChanged()
        LibraryRepository.onProfileChanged(profileIndex)
        WatchProgressRepository.onProfileChanged(profileIndex)
        AddonRepository.onProfileChanged(profileIndex)
        if (com.rovo.app.core.build.AppFeaturePolicy.pluginsEnabled) {
            PluginRepository.onProfileChanged(profileIndex)
        }
        ThemeSettingsRepository.onProfileChanged()
        PosterCardStyleRepository.onProfileChanged()
        PlayerSettingsRepository.onProfileChanged()
        HomeCatalogSettingsRepository.onProfileChanged()
        HomeRepository.clear()
        MetaScreenSettingsRepository.onProfileChanged()
        ContinueWatchingPreferencesRepository.onProfileChanged()
        EpisodeReleaseNotificationsRepository.onProfileChanged()
        TmdbSettingsRepository.onProfileChanged()
        MdbListSettingsRepository.onProfileChanged()
        TraktAuthRepository.onProfileChanged()
        SearchHistoryRepository.onProfileChanged()
        CollectionRepository.onProfileChanged()
        CollectionMobileSettingsRepository.onProfileChanged()
        DownloadsRepository.onProfileChanged()
    }

    suspend fun createProfile(
        name: String,
        avatarColorHex: String,
        avatarId: String? = null,
        avatarUrl: String? = null,
        usesPrimaryAddons: Boolean = false,
    ) {
        val existing = _state.value.profiles
        val nextIndex = ((1..4).toSet() - existing.map { it.profileIndex }.toSet()).minOrNull() ?: return

        val authState = AuthRepository.state.value as? AuthState.Authenticated ?: return
        val newProfile = RovoProfile(
            id = "",
            userId = authState.userId,
            profileIndex = nextIndex,
            name = name,
            avatarColorHex = avatarColorHex,
            avatarId = avatarId,
            avatarUrl = avatarUrl,
            usesPrimaryAddons = usesPrimaryAddons,
        )
        _state.value = _state.value.copy(
            profiles = (existing + newProfile).sortedBy { it.profileIndex },
            isLoaded = true,
        )
        syncPinCache(_state.value.profiles)
        persist()
    }

    suspend fun updateProfile(
        profileIndex: Int,
        name: String,
        avatarColorHex: String,
        avatarId: String? = null,
        avatarUrl: String? = null,
        usesPrimaryAddons: Boolean = false,
    ) {
        val authState = AuthRepository.state.value as? AuthState.Authenticated ?: return
        _state.value = _state.value.copy(
            profiles = _state.value.profiles.map { profile ->
                if (profile.profileIndex == profileIndex) {
                    RovoProfile(
                        id = profile.id,
                        userId = authState.userId,
                        profileIndex = profileIndex,
                        name = name,
                        avatarColorHex = avatarColorHex,
                        avatarId = avatarId,
                        avatarUrl = avatarUrl,
                        usesPrimaryAddons = usesPrimaryAddons,
                        usesPrimaryPlugins = profile.usesPrimaryPlugins,
                        updatedAt = "",
                    )
                } else {
                    profile
                }
            },
        )
        persist()
    }

    suspend fun deleteProfile(profileIndex: Int) {
        val remaining = _state.value.profiles.filter { it.profileIndex != profileIndex }
        ProfilePinCacheStorage.removePayload(profileIndex)
        _state.value = _state.value.copy(
            profiles = remaining,
            activeProfile = if (_state.value.activeProfile?.profileIndex == profileIndex) remaining.firstOrNull() else _state.value.activeProfile,
        )
        if (_state.value.activeProfile != null) {
            activeProfileIndex = _state.value.activeProfile!!.profileIndex
        }
        persist()
    }

    suspend fun verifyPin(profileIndex: Int, pin: String): PinVerifyResult {
        return verifyPinLocally(profileIndex, pin)
    }

    suspend fun setPin(profileIndex: Int, pin: String, currentPin: String? = null): PinVerifyResult {
        val profile = _state.value.profiles.find { it.profileIndex == profileIndex } ?: return PinVerifyResult(unlocked = false)
        val updatedProfile = profile.copy(pinEnabled = true, updatedAt = "")
        _state.value = _state.value.copy(
            profiles = _state.value.profiles.map { if (it.profileIndex == profileIndex) updatedProfile else it },
        )
        rememberVerifiedPin(profileIndex = profileIndex, pin = pin)
        persist()
        return PinVerifyResult(unlocked = true)
    }

    suspend fun clearPin(profileIndex: Int, currentPin: String? = null): PinVerifyResult {
        val profile = _state.value.profiles.find { it.profileIndex == profileIndex } ?: return PinVerifyResult(unlocked = false)
        val updatedProfile = profile.copy(pinEnabled = false, updatedAt = "")
        _state.value = _state.value.copy(
            profiles = _state.value.profiles.map { if (it.profileIndex == profileIndex) updatedProfile else it },
        )
        ProfilePinCacheStorage.removePayload(profileIndex)
        persist()
        return PinVerifyResult(unlocked = true)
    }

    private fun decodeStoredPayload(): StoredProfilePayload? {
        val payload = ProfileStorage.loadPayload().orEmpty().trim()
        if (payload.isEmpty()) return null

        return runCatching {
            json.decodeFromString<StoredProfilePayload>(payload)
        }.getOrNull()
    }

    private fun applyStoredPayload(stored: StoredProfilePayload) {
        val profiles = stored.profiles.sortedBy { it.profileIndex }
        activeProfileIndex = stored.activeProfileIndex
        _state.value = ProfileState(
            profiles = profiles,
            activeProfile = profiles.find { it.profileIndex == activeProfileIndex } ?: profiles.firstOrNull(),
            isLoaded = profiles.isNotEmpty(),
        )
        _state.value.activeProfile?.let { activeProfileIndex = it.profileIndex }
        syncPinCache(profiles)
    }

    private fun rememberVerifiedPin(profileIndex: Int, pin: String) {
        val profile = _state.value.profiles.find { it.profileIndex == profileIndex }
        val salt = generateProfilePinSalt()
        val payload = CachedProfilePinPayload(
            salt = salt,
            digest = hashProfilePin(profileIndex = profileIndex, salt = salt, pin = pin),
            profileUpdatedAt = profile?.updatedAt.orEmpty(),
        )
        ProfilePinCacheStorage.savePayload(profileIndex, json.encodeToString(payload))
    }

    private fun verifyPinLocally(profileIndex: Int, pin: String): PinVerifyResult {
        val profile = _state.value.profiles.find { it.profileIndex == profileIndex }
        if (profile?.pinEnabled != true) {
            return PinVerifyResult(unlocked = true)
        }

        val payload = ProfilePinCacheStorage.loadPayload(profileIndex).orEmpty().trim()
        if (payload.isEmpty()) {
            return PinVerifyResult(
                unlocked = false,
                message = localizedString(Res.string.profile_pin_offline_verification_requires_online),
            )
        }

        val cached = runCatching {
            json.decodeFromString<CachedProfilePinPayload>(payload)
        }.getOrNull() ?: return PinVerifyResult(
            unlocked = false,
            message = localizedString(Res.string.profile_pin_offline_verification_requires_online),
        )

        if (
            cached.profileUpdatedAt.isNotBlank() &&
            profile.updatedAt.isNotBlank() &&
            cached.profileUpdatedAt != profile.updatedAt
        ) {
            ProfilePinCacheStorage.removePayload(profileIndex)
            return PinVerifyResult(
                unlocked = false,
                message = localizedString(Res.string.profile_pin_changed_requires_refresh),
            )
        }

        val digest = hashProfilePin(profileIndex = profileIndex, salt = cached.salt, pin = pin)
        return if (digest == cached.digest) {
            PinVerifyResult(unlocked = true)
        } else {
            PinVerifyResult(unlocked = false, message = localizedString(Res.string.pin_incorrect))
        }
    }

    private fun syncPinCache(profiles: List<RovoProfile>) {
        val profilesByIndex = profiles.associateBy { it.profileIndex }
        for (profileIndex in 1..4) {
            val profile = profilesByIndex[profileIndex]
            if (profile == null || !profile.pinEnabled) {
                ProfilePinCacheStorage.removePayload(profileIndex)
                continue
            }

            val raw = ProfilePinCacheStorage.loadPayload(profileIndex).orEmpty().trim()
            if (raw.isEmpty()) continue

            val cached = runCatching {
                json.decodeFromString<CachedProfilePinPayload>(raw)
            }.getOrNull() ?: run {
                ProfilePinCacheStorage.removePayload(profileIndex)
                continue
            }

            if (
                cached.profileUpdatedAt.isNotBlank() &&
                profile.updatedAt.isNotBlank() &&
                cached.profileUpdatedAt != profile.updatedAt
            ) {
                ProfilePinCacheStorage.removePayload(profileIndex)
            }
        }
    }

    private fun persist() {
        val authState = AuthRepository.state.value as? AuthState.Authenticated ?: return
        ProfileStorage.savePayload(
            json.encodeToString(
                StoredProfilePayload(
                    userId = authState.userId,
                    activeProfileIndex = activeProfileIndex,
                    profiles = _state.value.profiles,
                ),
            ),
        )
    }
}

@kotlinx.serialization.Serializable
data class ProfileLockState(
    @kotlinx.serialization.SerialName("profile_index") val profileIndex: Int,
    @kotlinx.serialization.SerialName("pin_enabled") val pinEnabled: Boolean = false,
    @kotlinx.serialization.SerialName("pin_locked_until") val pinLockedUntil: String? = null,
)
