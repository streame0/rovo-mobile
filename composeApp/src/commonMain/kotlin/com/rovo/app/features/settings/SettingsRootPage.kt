package com.rovo.app.features.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rovo.app.core.build.AppVersionConfig
import rovo.composeapp.generated.resources.Res
import rovo.composeapp.generated.resources.compose_about_made_with
import rovo.composeapp.generated.resources.compose_about_version_format
import rovo.composeapp.generated.resources.compose_settings_page_appearance
import rovo.composeapp.generated.resources.compose_settings_page_integrations
import rovo.composeapp.generated.resources.compose_settings_page_licenses_attributions
import rovo.composeapp.generated.resources.compose_settings_page_notifications
import rovo.composeapp.generated.resources.compose_settings_page_playback
import rovo.composeapp.generated.resources.compose_settings_page_supporters_contributors
import rovo.composeapp.generated.resources.compose_settings_root_appearance_description
import rovo.composeapp.generated.resources.compose_settings_root_check_updates_description
import rovo.composeapp.generated.resources.compose_settings_root_check_updates_title
import rovo.composeapp.generated.resources.compose_settings_root_content_discovery_description
import rovo.composeapp.generated.resources.compose_settings_root_downloads_description
import rovo.composeapp.generated.resources.compose_settings_root_downloads_title
import rovo.composeapp.generated.resources.compose_settings_root_general_section
import rovo.composeapp.generated.resources.compose_settings_root_integrations_description
import rovo.composeapp.generated.resources.compose_settings_root_notifications_description
import rovo.composeapp.generated.resources.compose_settings_root_switch_profile_description
import rovo.composeapp.generated.resources.compose_settings_root_switch_profile_title
import rovo.composeapp.generated.resources.compose_settings_root_trakt_description
import rovo.composeapp.generated.resources.compose_settings_root_about_section
import rovo.composeapp.generated.resources.compose_settings_page_content_discovery
import rovo.composeapp.generated.resources.compose_settings_page_trakt
import rovo.composeapp.generated.resources.settings_playback_subtitle
import rovo.composeapp.generated.resources.about_supporters_contributors_subtitle
import rovo.composeapp.generated.resources.about_licenses_attributions_subtitle
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.settingsRootContent(
    isTablet: Boolean,
    onPlaybackClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onContentDiscoveryClick: () -> Unit,
    onIntegrationsClick: () -> Unit,
    onTraktClick: () -> Unit,
    onSupportersContributorsClick: () -> Unit,
    onLicensesAttributionsClick: () -> Unit,
    onCheckForUpdatesClick: (() -> Unit)? = null,
    onDownloadsClick: () -> Unit,
    onSwitchProfileClick: (() -> Unit)? = null,
    showGeneralSection: Boolean = true,
    showAboutSection: Boolean = true,
) {
    if (showGeneralSection) {
        item {
            SettingsSection(
                title = stringResource(Res.string.compose_settings_root_general_section),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_appearance),
                        description = stringResource(Res.string.compose_settings_root_appearance_description),
                        icon = Icons.Rounded.Palette,
                        isTablet = isTablet,
                        onClick = onAppearanceClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_content_discovery),
                        description = stringResource(Res.string.compose_settings_root_content_discovery_description),
                        icon = Icons.Rounded.Extension,
                        isTablet = isTablet,
                        onClick = onContentDiscoveryClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_root_downloads_title),
                        description = stringResource(Res.string.compose_settings_root_downloads_description),
                        icon = Icons.Rounded.Download,
                        isTablet = isTablet,
                        onClick = onDownloadsClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_playback),
                        description = stringResource(Res.string.settings_playback_subtitle),
                        icon = Icons.Rounded.PlayArrow,
                        isTablet = isTablet,
                        onClick = onPlaybackClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_integrations),
                        description = stringResource(Res.string.compose_settings_root_integrations_description),
                        icon = Icons.Rounded.Link,
                        isTablet = isTablet,
                        onClick = onIntegrationsClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_notifications),
                        description = stringResource(Res.string.compose_settings_root_notifications_description),
                        icon = Icons.Rounded.Notifications,
                        isTablet = isTablet,
                        onClick = onNotificationsClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_trakt),
                        description = stringResource(Res.string.compose_settings_root_trakt_description),
                        iconPainter = integrationLogoPainter(IntegrationLogo.Trakt),
                        isTablet = isTablet,
                        onClick = onTraktClick,
                    )
                }
            }
        }
    }
    if (showAboutSection) {
        item {
            SettingsSection(
                title = stringResource(Res.string.compose_settings_root_about_section),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_supporters_contributors),
                        description = stringResource(Res.string.about_supporters_contributors_subtitle),
                        icon = Icons.Rounded.Favorite,
                        isTablet = isTablet,
                        onClick = onSupportersContributorsClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_licenses_attributions),
                        description = stringResource(Res.string.about_licenses_attributions_subtitle),
                        icon = Icons.Rounded.Info,
                        isTablet = isTablet,
                        onClick = onLicensesAttributionsClick,
                    )
                    if (onCheckForUpdatesClick != null) {
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsNavigationRow(
                            title = stringResource(Res.string.compose_settings_root_check_updates_title),
                            description = stringResource(Res.string.compose_settings_root_check_updates_description),
                            icon = Icons.Rounded.Download,
                            isTablet = isTablet,
                            onClick = onCheckForUpdatesClick,
                        )
                    }
                }
            }
        }
    }
    item {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = if (isTablet) 20.dp else 16.dp),
        ) {
            Text(
                text = stringResource(Res.string.compose_about_made_with),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    Res.string.compose_about_version_format,
                    AppVersionConfig.VERSION_NAME,
                    AppVersionConfig.VERSION_CODE,
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
