package com.rovo.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import com.rovo.app.features.plugins.PluginsSettingsPageContent

internal actual fun LazyListScope.pluginsSettingsContent() {
    item {
        PluginsSettingsPageContent(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
