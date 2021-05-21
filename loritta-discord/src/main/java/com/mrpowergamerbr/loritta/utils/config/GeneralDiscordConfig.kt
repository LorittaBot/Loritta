package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class GeneralDiscordConfig(
		val discord: DiscordConfig,
		val shardController: ShardControllerConfig,
		val okHttp: JdaOkHttpConfig,
		val discordBots: DiscordBotsConfig,
		val discordBotList: DiscordBotListConfig,
		val antiRaidIds: List<String>,
		val messageEncryption: MessageEncryptionConfig
)