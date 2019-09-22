package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class GeneralDiscordConfig @JsonCreator constructor(
		val discord: DiscordConfig,
		val lavalink: LavalinkConfig,
		val discordBots: DiscordBotsConfig,
		val discordBotList: DiscordBotListConfig,
		val ghostIds: List<String>,
		val antiRaidIds: List<String>
)