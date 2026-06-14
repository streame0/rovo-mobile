package com.rovo.shared.model.introdb

import kotlinx.serialization.Serializable

@Serializable
data class IntroDbSegmentsResponse(
    val intro: IntroDbSegment? = null,
    val recap: IntroDbSegment? = null,
    val outro: IntroDbSegment? = null
)

@Serializable
data class IntroDbSegment(
    val start_ms: Long = 0,
    val end_ms: Long = 0,
    val confidence: Double? = null,
    val submission_count: Int? = null
)
