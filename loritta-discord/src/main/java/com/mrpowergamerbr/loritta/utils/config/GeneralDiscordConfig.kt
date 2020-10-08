package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class GeneralDiscordConfig @JsonCreator constructor(
		val discord: DiscordConfig,
		val shardController: ShardControllerConfig,
		val okHttp: JdaOkHttpConfig,
		val discordBots: DiscordBotsConfig,
		val discordBotList: DiscordBotListConfig,
		val antiRaidIds: List<String>,
		val messageEncryption: MessageEncryptionConfig
)