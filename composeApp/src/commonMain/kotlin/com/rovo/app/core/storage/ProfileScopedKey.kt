package com.rovo.app.core.storage

import com.rovo.app.features.profiles.ProfileRepository


object ProfileScopedKey {
    fun of(baseKey: String): String = "${baseKey}_${ProfileRepository.activeProfileId}"
}
