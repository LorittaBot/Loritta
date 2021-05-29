package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class GeneralDiscordInstanceConfig(
		val discord: DiscordInstanceConfig,
)