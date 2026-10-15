package com.project.lol.innertube.models.body

import com.project.lol.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class NextBody(
    val context: Context,
    val videoId: String,
)
