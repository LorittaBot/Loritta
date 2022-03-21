package net.perfectdreams.showtime.backend.utils.commands

import kotlinx.serialization.Serializable

@Serializable
class AdditionalCommandInfoConfig(
        val name: String,
        val imageUrls: List<String>? = null,
        val videoUrls: List<String>? = null
)