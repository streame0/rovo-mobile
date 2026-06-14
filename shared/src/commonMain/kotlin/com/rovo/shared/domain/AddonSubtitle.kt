package com.rovo.shared.domain

import kotlin.jvm.JvmInline

data class AddonSubtitle(
    val id: String,
    val url: String,
    val lang: String?,
    val addonName: String
)
