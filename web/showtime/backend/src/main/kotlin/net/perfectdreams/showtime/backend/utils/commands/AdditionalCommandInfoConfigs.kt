package net.perfectdreams.showtime.backend.utils.commands

import kotlinx.serialization.Serializable

@Serializable
class AdditionalCommandInfoConfigs(
        val additionalCommandInfos: List<AdditionalCommandInfoConfig>
)