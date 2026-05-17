package com.rovo.app.features.profiles

internal expect object ProfileHoverHapticFeedback {
    fun prepare()
    fun perform()
    fun release()
}
