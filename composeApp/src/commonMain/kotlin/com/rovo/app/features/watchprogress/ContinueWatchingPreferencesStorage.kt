package com.rovo.app.features.watchprogress

internal expect object ContinueWatchingPreferencesStorage {
    fun loadPayload(): String?
    fun savePayload(payload: String)
}
