package com.rovo.app.core.i18n

import kotlinx.coroutines.runBlocking
import rovo.composeapp.generated.resources.Res
import rovo.composeapp.generated.resources.action_play
import rovo.composeapp.generated.resources.action_play_episode
import rovo.composeapp.generated.resources.action_resume
import rovo.composeapp.generated.resources.action_resume_episode
import rovo.composeapp.generated.resources.compose_player_episode_code_episode_only
import rovo.composeapp.generated.resources.compose_player_episode_code_full
import rovo.composeapp.generated.resources.continue_watching_up_next
import rovo.composeapp.generated.resources.continue_watching_up_next_episode
import rovo.composeapp.generated.resources.date_month_april
import rovo.composeapp.generated.resources.date_month_august
import rovo.composeapp.generated.resources.date_month_december
import rovo.composeapp.generated.resources.date_month_february
import rovo.composeapp.generated.resources.date_month_january
import rovo.composeapp.generated.resources.date_month_july
import rovo.composeapp.generated.resources.date_month_june
import rovo.composeapp.generated.resources.date_month_march
import rovo.composeapp.generated.resources.date_month_may
import rovo.composeapp.generated.resources.date_month_november
import rovo.composeapp.generated.resources.date_month_october
import rovo.composeapp.generated.resources.date_month_september
import rovo.composeapp.generated.resources.date_month_short_apr
import rovo.composeapp.generated.resources.date_month_short_aug
import rovo.composeapp.generated.resources.date_month_short_dec
import rovo.composeapp.generated.resources.date_month_short_feb
import rovo.composeapp.generated.resources.date_month_short_jan
import rovo.composeapp.generated.resources.date_month_short_jul
import rovo.composeapp.generated.resources.date_month_short_jun
import rovo.composeapp.generated.resources.date_month_short_mar
import rovo.composeapp.generated.resources.date_month_short_may
import rovo.composeapp.generated.resources.date_month_short_nov
import rovo.composeapp.generated.resources.date_month_short_oct
import rovo.composeapp.generated.resources.date_month_short_sep
import rovo.composeapp.generated.resources.media_anime
import rovo.composeapp.generated.resources.media_channels
import rovo.composeapp.generated.resources.media_movie
import rovo.composeapp.generated.resources.media_movies
import rovo.composeapp.generated.resources.media_series
import rovo.composeapp.generated.resources.media_tv
import rovo.composeapp.generated.resources.unit_bytes_b
import rovo.composeapp.generated.resources.unit_bytes_gb
import rovo.composeapp.generated.resources.unit_bytes_kb
import rovo.composeapp.generated.resources.unit_bytes_mb
import org.jetbrains.compose.resources.getString

fun localizedMediaTypeLabel(type: String): String {
    val fallback = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    return when (type.trim().lowercase()) {
        "movie" -> resourceString("Movies") { getString(Res.string.media_movies) }
        "series" -> resourceString("Series") { getString(Res.string.media_series) }
        "anime" -> resourceString("Anime") { getString(Res.string.media_anime) }
        "channel" -> resourceString("Channels") { getString(Res.string.media_channels) }
        "tv" -> resourceString("TV") { getString(Res.string.media_tv) }
        else -> fallback
    }
}

fun localizedMovieTypeLabel(): String = resourceString("Movie") { getString(Res.string.media_movie) }

fun localizedSeasonEpisodeCode(seasonNumber: Int?, episodeNumber: Int?): String? =
    when {
        seasonNumber != null && episodeNumber != null ->
            resourceString("S${seasonNumber}E${episodeNumber}") {
                getString(Res.string.compose_player_episode_code_full, seasonNumber, episodeNumber)
            }
        episodeNumber != null ->
            resourceString("E${episodeNumber}") {
                getString(Res.string.compose_player_episode_code_episode_only, episodeNumber)
            }
        else -> null
    }

fun localizedPlayLabel(seasonNumber: Int?, episodeNumber: Int?): String {
    val episodeCode = localizedSeasonEpisodeCode(seasonNumber, episodeNumber)
    return if (episodeCode != null) {
        resourceString("Play $episodeCode") { getString(Res.string.action_play_episode, episodeCode) }
    } else {
        resourceString("Play") { getString(Res.string.action_play) }
    }
}

fun localizedResumeLabel(seasonNumber: Int?, episodeNumber: Int?): String {
    val episodeCode = localizedSeasonEpisodeCode(seasonNumber, episodeNumber)
    return if (episodeCode != null) {
        resourceString("Resume $episodeCode") { getString(Res.string.action_resume_episode, episodeCode) }
    } else {
        resourceString("Resume") { getString(Res.string.action_resume) }
    }
}

fun localizedUpNextLabel(seasonNumber: Int?, episodeNumber: Int?): String =
    if (seasonNumber != null && episodeNumber != null) {
        resourceString("Up Next • S${seasonNumber}E${episodeNumber}") {
            getString(Res.string.continue_watching_up_next_episode, seasonNumber, episodeNumber)
        }
    } else {
        resourceString("Up Next") { getString(Res.string.continue_watching_up_next) }
    }

fun localizedMonthName(month: Int): String =
    when (month) {
        1 -> resourceString("January") { getString(Res.string.date_month_january) }
        2 -> resourceString("February") { getString(Res.string.date_month_february) }
        3 -> resourceString("March") { getString(Res.string.date_month_march) }
        4 -> resourceString("April") { getString(Res.string.date_month_april) }
        5 -> resourceString("May") { getString(Res.string.date_month_may) }
        6 -> resourceString("June") { getString(Res.string.date_month_june) }
        7 -> resourceString("July") { getString(Res.string.date_month_july) }
        8 -> resourceString("August") { getString(Res.string.date_month_august) }
        9 -> resourceString("September") { getString(Res.string.date_month_september) }
        10 -> resourceString("October") { getString(Res.string.date_month_october) }
        11 -> resourceString("November") { getString(Res.string.date_month_november) }
        12 -> resourceString("December") { getString(Res.string.date_month_december) }
        else -> month.toString()
    }

fun localizedShortMonthName(month: Int): String =
    when (month) {
        1 -> resourceString("Jan") { getString(Res.string.date_month_short_jan) }
        2 -> resourceString("Feb") { getString(Res.string.date_month_short_feb) }
        3 -> resourceString("Mar") { getString(Res.string.date_month_short_mar) }
        4 -> resourceString("Apr") { getString(Res.string.date_month_short_apr) }
        5 -> resourceString("May") { getString(Res.string.date_month_short_may) }
        6 -> resourceString("Jun") { getString(Res.string.date_month_short_jun) }
        7 -> resourceString("Jul") { getString(Res.string.date_month_short_jul) }
        8 -> resourceString("Aug") { getString(Res.string.date_month_short_aug) }
        9 -> resourceString("Sep") { getString(Res.string.date_month_short_sep) }
        10 -> resourceString("Oct") { getString(Res.string.date_month_short_oct) }
        11 -> resourceString("Nov") { getString(Res.string.date_month_short_nov) }
        12 -> resourceString("Dec") { getString(Res.string.date_month_short_dec) }
        else -> month.toString()
    }

fun localizedByteUnit(unit: String): String =
    when (unit) {
        "GB" -> resourceString("GB") { getString(Res.string.unit_bytes_gb) }
        "MB" -> resourceString("MB") { getString(Res.string.unit_bytes_mb) }
        "KB" -> resourceString("KB") { getString(Res.string.unit_bytes_kb) }
        else -> resourceString("B") { getString(Res.string.unit_bytes_b) }
    }

private fun resourceString(
    fallback: String,
    provider: suspend () -> String,
): String = runCatching {
    runBlocking { provider() }
}.getOrDefault(fallback)
