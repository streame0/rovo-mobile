package com.rovo.app.core.native

actual object NativeAddonBridge {
    actual fun isAvailable(): Boolean = false

    actual suspend fun fetchCatalogJson(
        transportBaseUrl: String,
        contentType: String,
        catalogId: String,
        extraJson: String,
    ): String? = null

    actual suspend fun fetchMetaJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
    ): String? = null

    actual suspend fun fetchStreamsJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
    ): String? = null

    actual suspend fun fetchSubtitlesJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
        extraJson: String,
    ): String? = null

    actual suspend fun fetchManifestJson(transportBaseUrl: String): String? = null

    actual suspend fun extractInfoHash(magnet: String): String? = null

    actual suspend fun isValidUrl(url: String): Boolean = false
}
