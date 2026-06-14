package com.rovo.shared.model.stremio

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class MetaResponse(
    val meta: MetaItem
)

@Serializable
data class MetaItem(
    val id: String = "",
    val type: String = "", // "movie", "series"
    val name: String = "",
    val poster: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: String? = null,
    val runtime: String? = null,
    val genres: List<String>? = null,
    val videos: List<MetaVideo>? = null,
    val cast: List<MetaCast>? = null,
    val trailerKey: String? = null,
    @Transient val progress: Float = 0f,
    @Transient val addonBaseUrl: String? = null,
    @Transient val hasNewEpisode: Boolean = false
)

@Serializable(with = MetaCastSerializer::class)
data class MetaCast(
    val name: String,
    val character: String? = null,
    val profilePath: String? = null
)

object MetaCastSerializer : KSerializer<MetaCast> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MetaCast", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MetaCast {
        val input = decoder as? JsonDecoder ?: throw Exception("Only JSON is supported")
        val element = input.decodeJsonElement()
        return if (element is JsonPrimitive) {
            MetaCast(name = element.content)
        } else {
            val jsonObject = element.jsonObject
            MetaCast(
                name = jsonObject["name"]?.jsonPrimitive?.content ?: "",
                character = jsonObject["character"]?.jsonPrimitive?.contentOrNull,
                profilePath = jsonObject["profilePath"]?.jsonPrimitive?.contentOrNull
            )
        }
    }

    override fun serialize(encoder: Encoder, value: MetaCast) {
        val output = encoder as? JsonEncoder ?: throw Exception("Only JSON is supported")
        output.encodeJsonElement(buildJsonObject {
            put("name", value.name)
            value.character?.let { put("character", it) }
            value.profilePath?.let { put("profilePath", it) }
        })
    }
}

@Serializable
data class MetaVideo(
    val id: String = "",
    @SerialName("title")
    val title: String = "Episode",
    val released: String? = null,
    val thumbnail: String? = null,
    val overview: String? = null,
    val season: Int = 0,
    val episode: Int = 0
)

@Serializable
data class CatalogResponse(
    val metas: List<MetaItem>? = null,
    val hasMore: Boolean? = null
)
