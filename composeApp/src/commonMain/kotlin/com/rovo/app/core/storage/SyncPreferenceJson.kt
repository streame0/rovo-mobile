package com.rovo.app.core.storage

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

fun encodeSyncBoolean(value: Boolean): JsonElement = JsonPrimitive(value)
fun encodeSyncString(value: String): JsonElement = JsonPrimitive(value)
fun encodeSyncInt(value: Int): JsonElement = JsonPrimitive(value)
fun encodeSyncFloat(value: Float): JsonElement = JsonPrimitive(value.toDouble())
fun encodeSyncStringSet(value: Set<String>): JsonElement = JsonArray(value.map { JsonPrimitive(it) })

fun JsonObject.decodeSyncBoolean(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull
fun JsonObject.decodeSyncString(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
fun JsonObject.decodeSyncInt(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull
fun JsonObject.decodeSyncFloat(key: String): Float? = this[key]?.jsonPrimitive?.doubleOrNull?.toFloat()
fun JsonObject.decodeSyncStringSet(key: String): Set<String>? =
    this[key]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
