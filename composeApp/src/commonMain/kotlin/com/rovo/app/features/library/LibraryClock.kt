package com.rovo.app.features.library

internal expect object LibraryClock {
    fun nowEpochMs(): Long
}
