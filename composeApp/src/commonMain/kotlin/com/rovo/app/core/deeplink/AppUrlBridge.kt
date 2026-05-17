package com.rovo.app.core.deeplink

import com.rovo.app.features.trakt.handleTraktAuthCallbackUrl
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AppDeepLink {
    data class Meta(
        val type: String,
        val id: String,
    ) : AppDeepLink

    data object Downloads : AppDeepLink
}

object AppDeepLinkRepository {
    private val _pendingDeepLink = MutableStateFlow<AppDeepLink?>(null)
    val pendingDeepLink: StateFlow<AppDeepLink?> = _pendingDeepLink.asStateFlow()

    fun handleUrl(url: String) {
        parseAppDeepLink(url)?.let { deepLink ->
            _pendingDeepLink.value = deepLink
        }
    }

    fun markConsumed(deepLink: AppDeepLink) {
        if (_pendingDeepLink.value == deepLink) {
            _pendingDeepLink.value = null
        }
    }
}

fun handleAppUrl(url: String) {
    val normalizedUrl = url.trim()
    if (normalizedUrl.isBlank()) return

    handleTraktAuthCallbackUrl(normalizedUrl)
    AppDeepLinkRepository.handleUrl(normalizedUrl)
}

fun buildMetaDeepLinkUrl(
    type: String,
    id: String,
): String = buildString {
    append("rovo://meta?type=")
    append(type.trim().encodeURLParameter())
    append("&id=")
    append(id.trim().encodeURLParameter())
}

fun buildDownloadsDeepLinkUrl(): String = "rovo://downloads"

private fun parseAppDeepLink(url: String): AppDeepLink? {
    val parsedUrl = runCatching { Url(url) }.getOrNull() ?: return null
    if (!parsedUrl.protocol.name.equals("rovo", ignoreCase = true)) return null

    return when (parsedUrl.host.lowercase()) {
        "meta" -> {
            val type = parsedUrl.parameters["type"]?.trim().orEmpty()
            val id = parsedUrl.parameters["id"]?.trim().orEmpty()
            if (type.isBlank() || id.isBlank()) null else AppDeepLink.Meta(type = type, id = id)
        }

        "downloads" -> AppDeepLink.Downloads

        else -> null
    }
}