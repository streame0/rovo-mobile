package com.rovo.app.core.network

enum class NetworkCondition {
    Unknown,
    Checking,
    Online,
    NoInternet,
    ServersUnreachable,
}

fun NetworkCondition.titleForEmptyState(): String = when (this) {
    NetworkCondition.NoInternet -> "No Internet Connection"
    NetworkCondition.ServersUnreachable -> "Servers Unreachable"
    NetworkCondition.Unknown -> "Checking connection..."
    NetworkCondition.Checking -> "Checking connection..."
    NetworkCondition.Online -> "No results"
}

fun NetworkCondition.messageForEmptyState(): String = when (this) {
    NetworkCondition.NoInternet -> "Please check your internet connection and try again."
    NetworkCondition.ServersUnreachable -> "The server may be down or blocked. Please try again later."
    NetworkCondition.Unknown -> "Waiting for network status..."
    NetworkCondition.Checking -> "Checking network connectivity..."
    NetworkCondition.Online -> "Nothing to show here yet."
}
