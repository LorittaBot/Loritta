package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class DiscordInstanceConfig(
		val addBotUrl: String,
		val authorizationUrl: String
)