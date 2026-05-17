package com.rovo.app.features.profiles

internal expect object ProfilePinCrypto {
    fun sha256Hex(value: String): String
}