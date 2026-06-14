package com.rovo.shared.repository

import com.rovo.shared.api.StremioApi
import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.AddonEntity
import com.rovo.shared.data.local.entity.CatalogConfigEntity
import com.rovo.shared.model.stremio.CatalogManifest
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.Stream
import com.rovo.shared.domain.HomeRow
import com.rovo.shared.domain.HubGroupRow
import com.rovo.shared.domain.HubItem
import com.rovo.shared.domain.HubShape
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class AddonRepository(
    private val api: StremioApi,
    private val dao: AddonDao,
    private val json: Json
) {
    private val CATALOG_TIMEOUT_MS = 10_000L
    private val STREAM_TIMEOUT_MS = 20_000L

    private fun List<MetaItem>.sanitize(): List<MetaItem> = filter { item ->
        item.id.isNotEmpty() && item.name.isNotEmpty()
    }

    private fun MetaItem?.sanitize(): MetaItem? =
        this?.takeIf { it.id.isNotEmpty() && it.name.isNotEmpty() }

    suspend fun fetchNextCatalogPage(baseUrl: String, skip: Int): List<MetaItem> = withContext(Dispatchers.Default) {
        try {
            val url = if (skip == 0) baseUrl else {
                baseUrl.replace(".json", "/skip=$skip.json")
            }
            withTimeout(CATALOG_TIMEOUT_MS) { api.getCatalog(url) }.metas.orEmpty().sanitize()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun searchMovies(query: String): List<MetaItem> = withContext(Dispatchers.Default) {
        if (query.length < 3) return@withContext emptyList()

        val movieJob = async {
            try {
                val encodedQuery = query.replace(" ", "%20")
                withTimeout(CATALOG_TIMEOUT_MS) {
                    api.getCatalog("https://v3-cinemeta.strem.io/catalog/movie/top/search=$encodedQuery.json")
                }.metas.orEmpty().sanitize()
            } catch (e: Exception) { emptyList() }
        }

        val seriesJob = async {
            try {
                val encodedQuery = query.replace(" ", "%20")
                withTimeout(CATALOG_TIMEOUT_MS) {
                    api.getCatalog("https://v3-cinemeta.strem.io/catalog/series/top/search=$encodedQuery.json")
                }.metas.orEmpty().sanitize()
            } catch (e: Exception) { emptyList() }
        }

        val cinemeta = "https://v3-cinemeta.strem.io"
        return@withContext (movieJob.await() + seriesJob.await())
            .map { it.copy(addonBaseUrl = cinemeta) }
    }

    private fun catalogSupportsSkip(addon: AddonEntity, catalogType: String, catalogId: String): Boolean {
        val catalogs: List<CatalogManifest> = try {
            json.decodeFromString(addon.catalogsJson)
        } catch (_: Exception) { return false }
        val catalog = catalogs.find { it.type == catalogType && it.id == catalogId } ?: return false
        return catalog.extra?.any { it.name == "skip" } ?: false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDashboardRowsFlow(
        screen: String
    ): Flow<List<HomeRow>> = combine(
        dao.getAllAddons(),
        dao.getAllCatalogConfigs()
    ) { addons, configs ->
        addons to configs
    }.flatMapLatest { (addons, configs) ->
        flow {
            emit(getDashboardRows(screen, addons, configs))
        }
    }

    suspend fun getDashboardRows(
        screen: String,
        addons: List<AddonEntity>? = null,
        configs: List<CatalogConfigEntity>? = null,
        skipConfigs: Int = 0,
        maxConfigs: Int = Int.MAX_VALUE
    ): List<HomeRow> = withContext(Dispatchers.Default) {
        val currentAddons = addons ?: (dao.getAllAddons().firstOrNull()?.filter { it.isEnabled } ?: emptyList())
        val currentConfigs = configs ?: (dao.getAllCatalogConfigs().firstOrNull() ?: emptyList())
        val addonMap = currentAddons.filter { it.isEnabled }.associateBy { normalizeUrl(it.transportUrl) }

        val filteredConfigs = currentConfigs
            .filter { config ->
                val normalizedUrl = normalizeUrl(config.transportUrl)
                addonMap.containsKey(normalizedUrl) && when(screen) {
                    "home" -> config.showInHome
                    "movies" -> config.showInMovies
                    "series" -> config.showInSeries
                    else -> false
                }
            }
            .sortedBy { config ->
                when(screen) {
                    "home" -> config.homeOrder
                    "movies" -> config.moviesOrder
                    "series" -> config.seriesOrder
                    else -> 0
                }
            }
            .drop(skipConfigs.coerceAtLeast(0))
            .let { sliced ->
                if (maxConfigs == Int.MAX_VALUE) sliced else sliced.take(maxConfigs.coerceAtLeast(0))
            }

        println("AddonRepository: getDashboardRows for screen=$screen. Total configs: ${currentConfigs.size}, Filtered: ${filteredConfigs.size}. Enabled Addons: ${addonMap.keys}")

        val deferredJobs = filteredConfigs.mapNotNull { config ->
            val normalizedUrl = normalizeUrl(config.transportUrl)
            val addon = addonMap[normalizedUrl] ?: run {
                println("AddonRepository: No enabled addon found for $normalizedUrl (original: ${config.transportUrl})")
                return@mapNotNull null
            }
            async {
                try {
                    val baseUrl = normalizedUrl
                    val url = "$baseUrl/catalog/${config.catalogType}/${config.catalogId}.json"
                    println("AddonRepository: Fetching catalog for ${config.catalogName}: $url")
                    val rawMetas = try { 
                        withTimeout(CATALOG_TIMEOUT_MS) { api.getCatalog(url) }.metas.orEmpty().sanitize() 
                    } catch (e: Exception) { 
                        println("AddonRepository: Failed to fetch catalog $url: ${e.message}")
                        emptyList() 
                    }
                    if (rawMetas.isNotEmpty()) {
                        println("AddonRepository: Successfully fetched ${rawMetas.size} items for ${config.catalogName}")
                        val metas = rawMetas.map { it.copy(addonBaseUrl = normalizedUrl) }
                        HomeRow(
                            configId = config.uniqueId,
                            title = config.customTitle ?: config.catalogName ?: config.addonName,
                            items = metas,
                            catalogUrl = url,
                            isInfiniteLoopEnabled = config.isInfiniteLoopEnabled,
                            visibleItemCount = config.visibleItemCount,
                            isInfiniteScrollingEnabled = config.isInfiniteScrollingEnabled,
                            order = when(screen) {
                                "home" -> config.homeOrder
                                "movies" -> config.moviesOrder
                                "series" -> config.seriesOrder
                                else -> 999
                            },
                            supportsSkip = catalogSupportsSkip(addon, config.catalogType, config.catalogId)
                        )
                    } else {
                        println("AddonRepository: No metas found for ${config.catalogName} ($url)")
                        null
                    }
                } catch (e: Exception) { 
                    println("AddonRepository: Fatal error fetching ${config.catalogName}: ${e.message}")
                    null 
                }
            }
        }
        deferredJobs.awaitAll().filterNotNull()
    }

    suspend fun getStreams(type: String, id: String): List<Stream> = withContext(Dispatchers.Default) {
        val addons = dao.getAllAddons().firstOrNull()
            ?.filter { it.isEnabled && it.supportsStream }
            ?: emptyList()

        val jobs = addons.map { addon ->
            async {
                try {
                    val url = "${addon.transportUrl}/stream/$type/$id.json"
                    val response = withTimeout(STREAM_TIMEOUT_MS) { api.getStreams(url) }
                    val sourceLabel = addon.nickname ?: addon.name
                    response.streams.orEmpty().map { stream ->
                        stream.copy(
                            name = "[$sourceLabel] ${stream.name ?: ""}".trim(),
                            addonTransportUrl = addon.transportUrl
                        )
                    }
                } catch (e: Exception) { emptyList<Stream>() }
            }
        }

        jobs.awaitAll().flatten()
    }

    suspend fun installAddon(url: String, isTrusted: Boolean = false) = withContext(Dispatchers.Default) {
        val normalizedUrl = normalizeUrl(url)
        val manifestUrl = "$normalizedUrl/manifest.json"
        println("AddonRepository: Installing addon from $manifestUrl (original: $url)")
        
        val manifest = try {
            api.getManifest(manifestUrl)
        } catch (e: Exception) {
            println("AddonRepository: Failed to fetch manifest from $manifestUrl: ${e.message}")
            // Try the original URL if manifestUrl fails
            api.getManifest(url.trim().trimEnd('/'))
        }
        
        val transportUrl = normalizedUrl
        
        if (dao.getAddon(transportUrl) != null) {
            println("AddonRepository: Addon $transportUrl already installed, updating catalogs...")
        }

        val catalogsJson = json.encodeToString(manifest.catalogs.orEmpty())
        
        // Simplified resource checking for KMP migration
        val supportsMeta = manifest.resources?.any { it.toString().contains("meta") } ?: false
        val supportsStream = manifest.resources?.any { it.toString().contains("stream") } ?: false

        val entity = AddonEntity(
            transportUrl = transportUrl, id = manifest.id, name = manifest.name, version = manifest.version,
            description = manifest.description, iconUrl = manifest.logo, isTrusted = isTrusted, isEnabled = true,
            nickname = null, catalogsJson = catalogsJson,
            supportsMeta = supportsMeta,
            supportsStream = supportsStream,
            typesJson = json.encodeToString(manifest.types.orEmpty()),
            idPrefixesJson = json.encodeToString(manifest.idPrefixes.orEmpty())
        )
        dao.insertAddon(entity)

        val newConfigs = manifest.catalogs.orEmpty().map { catalog ->
            val uniqueId = "${transportUrl}/${catalog.type}/${catalog.id}"
            CatalogConfigEntity(
                uniqueId = uniqueId, transportUrl = transportUrl, addonName = manifest.name,
                catalogType = catalog.type, catalogId = catalog.id,
                catalogName = catalog.name, customTitle = null,
                showInHome = true, showInMovies = catalog.type == "movie", showInSeries = catalog.type == "series",
                homeOrder = 999, moviesOrder = 999, seriesOrder = 999
            )
        }
        println("AddonRepository: Saving ${newConfigs.size} catalog configs for ${manifest.name}")
        dao.saveCatalogConfigs(newConfigs)
    }

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .trimEnd('/')
            .removeSuffix("/manifest.json")
            .trimEnd('/')
    }

    suspend fun getDetails(
        type: String,
        id: String,
        preferredAddonBaseUrl: String? = null
    ): MetaItem = withContext(Dispatchers.Default) {
        if (!preferredAddonBaseUrl.isNullOrBlank()) {
            try {
                val url = "${preferredAddonBaseUrl.trimEnd('/')}/meta/$type/$id.json"
                val meta = withTimeout(5000L) { api.getMeta(url) }.meta.sanitize()
                if (meta != null) return@withContext meta
            } catch (_: Exception) { }
        }

        // Default fallback to Cinemeta
        try {
            val url = "https://v3-cinemeta.strem.io/meta/$type/$id.json"
            withTimeout(CATALOG_TIMEOUT_MS) { api.getMeta(url) }.meta.sanitize()
                ?: throw Exception("Meta not found")
        } catch (e: Exception) {
            throw e
        }
    }
}
