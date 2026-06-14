package com.rovo.app

import androidx.compose.ui.window.ComposeUIViewController
import com.rovo.shared.ui.settings.SettingsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIViewController

fun MainViewController() = ComposeUIViewController {
    App()
}

object TraktAuthHandler : KoinComponent {
    private val settingsViewModel: SettingsViewModel by inject()

    fun handleDeepLink(url: NSURL): Boolean {
        val components = NSURLComponents(uRL = url, resolvingAgainstBaseURL = false)
        if (components.scheme == "rovo" && components.host == "trakt") {
            val queryItems = components.queryItems as? List<NSURLQueryItem>
            val code = queryItems?.find { it.name == "code" }?.value
            if (code != null) {
                settingsViewModel.handleTraktCode(code, "rovo://trakt", "YOUR_TRAKT_CLIENT_SECRET")
                return true
            }
        }
        return false
    }
}
