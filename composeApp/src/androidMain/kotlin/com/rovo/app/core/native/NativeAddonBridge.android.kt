package com.rovo.app.core.native

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object NativeAddonBridge {
    private val available: Boolean by lazy {
        runCatching {
            uniffi.rovo_core.rovoVersion()
            true
        }.getOrDefault(false)
    }

    actual fun isAvailable(): Boolean = available

    actual suspend fun fetchCatalogJson(
        transportBaseUrl: String,
        contentType: String,
        catalogId: String,
        extraJson: String,
    ): String? = withContext(Dispatchers.IO) {
        if (!available) return@withContext null
        runCatching {
            uniffi.rovo_core.fetchCatalog(
                transportBaseUrl,
                contentType,
                catalogId,
                extraJson,
            ).nativeAddonPayloadOrNull()?.let { """{"metas":$it}""" }
        }.getOrNull()
    }

    actual suspend fun fetchMetaJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
    ): String? = withContext(Dispatchers.IO) {
        if (!available) return@withContext null
        runCatching {
            uniffi.rovo_core.fetchMeta(transportBaseUrl, contentType, id).nativeAddonPayloadOrNull()
        }.getOrNull()
    }

    actual suspend fun fetchStreamsJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
    ): String? = withContext(Dispatchers.IO) {
        if (!available) return@withContext null
        runCatching {
            uniffi.rovo_core.fetchStreams(transportBaseUrl, contentType, id)
                .nativeAddonPayloadOrNull()
                ?.let { """{"streams":$it}""" }
        }.getOrNull()
    }

    actual suspend fun fetchSubtitlesJson(
        transportBaseUrl: String,
        contentType: String,
        id: String,
        extraJson: String,
    ): String? = withContext(Dispatchers.IO) {
        if (!available) return@withContext null
        runCatching {
            uniffi.rovo_core.fetchSubtitles(transportBaseUrl, contentType, id, extraJson)
                .nativeAddonPayloadOrNull()
                ?.let { """{"subtitles":$it}""" }
        }.getOrNull()
    }

    actual suspend fun fetchManifestJson(transportBaseUrl: String): String? =
        withContext(Dispatchers.IO) {
            if (!available) return@withContext null
            runCatching {
                uniffi.rovo_core.fetchAddonManifest(transportBaseUrl).nativeAddonPayloadOrNull()
            }.getOrNull()
        }

    actual suspend fun extractInfoHash(magnet: String): String? = withContext(Dispatchers.IO) {
        if (!available) return@withContext null
        runCatching { uniffi.rovo_core.extractInfoHash(magnet) }.getOrNull()
    }

    actual suspend fun isValidUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        if (!available) return@withContext false
        runCatching { uniffi.rovo_core.isValidUrl(url) }.getOrDefault(false)
    }
}
