package com.rovo.app.features.watchprogress

internal expect object WatchProgressClock {
    fun nowEpochMs(): Long
}
