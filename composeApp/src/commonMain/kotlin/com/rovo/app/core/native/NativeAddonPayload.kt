package com.rovo.app.core.native

/** Returns null when rovo-core responded with `{"error": "..."}`. */
internal fun String.nativeAddonPayloadOrNull(): String? {
    if (trimStart().startsWith("{\"error\"")) return null
    return this
}
