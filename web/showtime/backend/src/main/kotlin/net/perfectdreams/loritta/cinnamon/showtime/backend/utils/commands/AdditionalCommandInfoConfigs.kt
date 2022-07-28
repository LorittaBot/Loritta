package net.perfectdreams.loritta.cinnamon.showtime.backend.utils.commands

import kotlinx.serialization.Serializable

@Serializable
class AdditionalCommandInfoConfigs(
        val additionalCommandInfos: List<AdditionalCommandInfoConfig>
)