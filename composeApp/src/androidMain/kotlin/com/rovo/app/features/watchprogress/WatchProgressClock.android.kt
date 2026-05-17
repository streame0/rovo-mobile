package com.rovo.app.features.watchprogress

actual object WatchProgressClock {
    actual fun nowEpochMs(): Long = System.currentTimeMillis()
}
