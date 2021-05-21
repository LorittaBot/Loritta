package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordInstanceConfig(
		@SerialName("add-bot-url")
		val addBotUrl: String,
		@SerialName("authorization-url")
		val authorizationUrl: String
)