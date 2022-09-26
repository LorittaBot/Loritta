package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class GeneralDiscordInstanceConfig(
		val discord: DiscordInstanceConfig,
)