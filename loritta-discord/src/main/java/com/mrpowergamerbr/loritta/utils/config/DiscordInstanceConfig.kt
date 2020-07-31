package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class DiscordInstanceConfig @JsonCreator constructor(
		val addBotUrl: String,
		val authorizationUrl: String
)