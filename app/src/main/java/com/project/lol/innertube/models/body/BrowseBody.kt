package com.project.lol.innertube.models.body

import com.project.lol.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String,
    val params: String? = null,
)
