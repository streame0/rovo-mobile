package com.rovo.shared.model.stremio

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Manifest(
    val id: String = "",
    val name: String = "",
    val version: String = "",
    val description: String? = null,
    val resources: List<JsonElement>? = null,
    val types: List<String>? = null,
    val catalogs: List<CatalogManifest>? = null,
    val logo: String? = null,
    val idPrefixes: List<String>? = null
)

@Serializable
data class CatalogManifest(
    val type: String = "",
    val id: String = "",
    val name: String = "",
    val extra: List<CatalogExtra>? = null
)

@Serializable
data class CatalogExtra(
    val name: String = "",
    val isRequired: Boolean = false,
    val options: List<String>? = null
)
