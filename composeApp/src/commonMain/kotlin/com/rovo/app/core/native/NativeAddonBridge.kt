package com.rovo.app.core.native

/**
 * Optional Android native addon transport (rovo-core / UniFFI).
 * Returns null when the native library is unavailable (iOS, desktop, or missing jniLibs).
 */
expect object NativeAddonBridge {
    fun isAvailable(): Boolean

    suspend fun fetchCatalogJson(
        transportBaseUrl: String,
        contentType: String,
        catalogId: String,
        extraJson: String,
    ): String?

    suspend fun fetchMetaJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
    ): String?

    suspend fun fetchStreamsJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
    ): String?

    suspend fun fetchSubtitlesJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
        extraJson: String = "{}",
    ): String?

    suspend fun fetchManifestJson(transportBaseUrl: String): String?

    suspend fun extractInfoHash(magnet: String): String?

    suspend fun isValidUrl(url: String): Boolean
}
