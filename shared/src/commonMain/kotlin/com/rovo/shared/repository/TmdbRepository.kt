package com.rovo.shared.repository

import com.rovo.shared.api.TmdbApi
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.MetaVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class TmdbRepository(private val api: TmdbApi) {
    private val imageBaseUrl = "https://image.tmdb.org/t/p/original"

    suspend fun enrich(meta: MetaItem, language: String): MetaItem = withContext(Dispatchers.Default) {
        try {
            val type = if (meta.type == "series") "tv" else "movie"
            val findResult = api.findByExternalId(meta.id)
            val tmdbId = if (type == "tv") findResult.tv_results.firstOrNull()?.id else findResult.movie_results.firstOrNull()?.id
            
            if (tmdbId == null) return@withContext meta

            val enriched = if (type == "tv") {
                val details = api.getTvDetails(tmdbId, language)
                meta.copy(
                    name = details.name,
                    description = details.overview ?: meta.description,
                    poster = details.poster_path?.let { "$imageBaseUrl$it" } ?: meta.poster,
                    background = details.backdrop_path?.let { "$imageBaseUrl$it" } ?: meta.background,
                    genres = details.genres?.map { it.name } ?: meta.genres,
                    imdbRating = details.vote_average?.let { ((it * 10).toInt() / 10f).toString() } ?: meta.imdbRating,
                    cast = details.credits?.cast?.take(10)?.map { 
                        com.rovo.shared.model.stremio.MetaCast(
                            name = it.name,
                            character = it.character,
                            profilePath = it.profile_path?.let { p -> "$imageBaseUrl$p" }
                        )
                    }
                )
            } else {
                val details = api.getMovieDetails(tmdbId, language)
                meta.copy(
                    name = details.title,
                    description = details.overview ?: meta.description,
                    poster = details.poster_path?.let { "$imageBaseUrl$it" } ?: meta.poster,
                    background = details.backdrop_path?.let { "$imageBaseUrl$it" } ?: meta.background,
                    genres = details.genres?.map { it.name } ?: meta.genres,
                    imdbRating = details.vote_average?.let { ((it * 10).toInt() / 10f).toString() } ?: meta.imdbRating,
                    runtime = details.runtime?.let { "${it}m" } ?: meta.runtime,
                    cast = details.credits?.cast?.take(10)?.map { 
                        com.rovo.shared.model.stremio.MetaCast(
                            name = it.name,
                            character = it.character,
                            profilePath = it.profile_path?.let { p -> "$imageBaseUrl$p" }
                        )
                    }
                )
            }
            enriched
        } catch (e: Exception) {
            meta
        }
    }

    suspend fun getRecommendations(type: String, id: String, language: String): List<MetaItem> = withContext(Dispatchers.Default) {
        try {
            val mediaType = if (type == "series") "tv" else "movie"
            val findResult = api.findByExternalId(id)
            val tmdbId = if (mediaType == "tv") findResult.tv_results.firstOrNull()?.id else findResult.movie_results.firstOrNull()?.id
            
            if (tmdbId == null) return@withContext emptyList()
            
            api.getRecommendations(mediaType, tmdbId, language).results.map { 
                MetaItem(
                    id = "tmdb:${it.id}", // Temporary ID or resolve to IMDb if needed
                    type = type,
                    name = it.title,
                    poster = it.poster_path?.let { p -> "$imageBaseUrl$p" },
                    background = it.backdrop_path?.let { b -> "$imageBaseUrl$b" }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrailer(type: String, id: String, language: String): String? = withContext(Dispatchers.Default) {
        try {
            val mediaType = if (type == "series") "tv" else "movie"
            val findResult = api.findByExternalId(id)
            val tmdbId = if (mediaType == "tv") findResult.tv_results.firstOrNull()?.id else findResult.movie_results.firstOrNull()?.id
            
            if (tmdbId == null) return@withContext null
            
            val videos = api.getVideos(mediaType, tmdbId, language)
            videos.results.find { it.site == "YouTube" && it.type == "Trailer" }?.key
        } catch (e: Exception) {
            null
        }
    }
}
