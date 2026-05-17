package com.rovo.app.features.trakt

import android.content.Context
import android.content.SharedPreferences
import com.rovo.app.core.storage.ProfileScopedKey

internal actual object TraktAuthStorage {
    private const val preferencesName = "rovo_trakt_auth"
    private const val payloadKey = "trakt_auth_payload"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    actual fun loadPayload(): String? =
        preferences?.getString(ProfileScopedKey.of(payloadKey), null)

    actual fun savePayload(payload: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(payloadKey), payload)
            ?.apply()
    }
}
