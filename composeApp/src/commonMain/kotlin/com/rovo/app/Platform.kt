package com.rovo.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

internal expect val isIos: Boolean