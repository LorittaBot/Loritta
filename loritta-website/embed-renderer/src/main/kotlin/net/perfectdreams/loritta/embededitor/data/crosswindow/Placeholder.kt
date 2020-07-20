package net.perfectdreams.loritta.embededitor.data.crosswindow

import kotlinx.serialization.Serializable

@Serializable
data class Placeholder(
        val placeholder: String,
        val replaceWith: String,
        val renderType: RenderType,
        val hidden: Boolean
)