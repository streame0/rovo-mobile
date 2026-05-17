package com.rovo.app.features.downloads

internal expect object DownloadsClock {
    fun nowEpochMs(): Long
}
