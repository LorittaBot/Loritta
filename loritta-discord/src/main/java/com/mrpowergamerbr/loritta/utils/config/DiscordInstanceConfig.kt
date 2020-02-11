package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class DiscordInstanceConfig @JsonCreator constructor(
		var minShardId: Int,
		var maxShardId: Int,
		val addBotUrl: String,
		val authorizationUrl: String
)