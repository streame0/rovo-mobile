package com.rovo.app.features.home

import com.rovo.app.core.i18n.localizedMediaTypeLabel
import com.rovo.app.features.addons.ManagedAddon
import com.rovo.app.features.catalog.supportsPagination
import kotlinx.coroutines.runBlocking
import rovo.composeapp.generated.resources.Res
import rovo.composeapp.generated.resources.home_catalog_default_title
import org.jetbrains.compose.resources.getString

data class HomeCatalogDefinition(
    val key: String,
    val defaultTitle: String,
    val addonName: String,
    val manifestUrl: String,
    val type: String,
    val catalogId: String,
    val supportsPagination: Boolean,
)

fun buildHomeCatalogDefinitions(addons: List<ManagedAddon>): List<HomeCatalogDefinition> =
    addons.mapNotNull { addon ->
        val manifest = addon.manifest ?: return@mapNotNull null
        addon to manifest
    }.flatMap { (addon, manifest) ->
        manifest.catalogs
            .filter { catalog -> catalog.extra.none { it.isRequired } }
            .map { catalog ->
                HomeCatalogDefinition(
                    key = "${manifest.id}:${catalog.type}:${catalog.id}",
                    defaultTitle = runBlocking {
                        getString(
                            Res.string.home_catalog_default_title,
                            catalog.name,
                            localizedMediaTypeLabel(catalog.type),
                        )
                    },
                    addonName = addon.displayTitle,
                    manifestUrl = addon.manifestUrl,
                    type = catalog.type,
                    catalogId = catalog.id,
                    supportsPagination = catalog.supportsPagination(),
                )
            }
    }.distinctBy(HomeCatalogDefinition::key)

internal fun String.displayLabel(): String = localizedMediaTypeLabel(this)
