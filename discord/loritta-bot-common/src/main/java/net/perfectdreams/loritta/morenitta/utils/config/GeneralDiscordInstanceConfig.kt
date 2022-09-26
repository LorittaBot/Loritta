package net.perfectdreams.loritta.morenitta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class GeneralDiscordInstanceConfig(
		val discord: DiscordInstanceConfig,
)