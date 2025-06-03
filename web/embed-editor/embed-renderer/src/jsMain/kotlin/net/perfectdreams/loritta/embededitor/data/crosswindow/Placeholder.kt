package net.perfectdreams.loritta.embededitor.data.crosswindow

import kotlinx.serialization.Serializable

@Serializable
data class Placeholder(
        val name: String,
        val replaceWith: String,
        val description: String? = null,
        val renderType: RenderType,
        val hidden: Boolean
)