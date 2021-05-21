package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class DiscordInstanceConfig(
		val addBotUrl: String,
		val authorizationUrl: String
)