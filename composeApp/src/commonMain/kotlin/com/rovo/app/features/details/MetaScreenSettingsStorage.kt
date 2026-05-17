package com.rovo.app.features.details

internal expect object MetaScreenSettingsStorage {
    fun loadPayload(): String?
    fun savePayload(payload: String)
}