package com.rovo.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rovo.app.App
import com.rovo.shared.ui.settings.SettingsViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Try to enter PiP mode when user swipes up/home
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().build())
            }
        } catch (e: Exception) {
            // Player might not be active or other restriction
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        // You could notify the app/viewmodel here if needed to hide UI
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "rovo" && data.host == "trakt") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                settingsViewModel.handleTraktCode(code, "rovo://trakt", "YOUR_TRAKT_CLIENT_SECRET")
            }
        }
    }
}
