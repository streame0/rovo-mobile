package com.rovo.app.features.search

import android.content.Context
import android.content.SharedPreferences
import com.rovo.app.core.storage.ProfileScopedKey

actual object SearchHistoryStorage {
    private const val preferencesName = "rovo_search_history"
    private const val payloadKey = "search_history_payload"

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
