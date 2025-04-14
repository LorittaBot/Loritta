package net.perfectdreams.loritta.website.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
data class TooltipsConfig(
    val tooltips: List<TooltipConfig>
) {
    @Serializable
    data class TooltipConfig(
        val match: String,
        val content: String
    )
}