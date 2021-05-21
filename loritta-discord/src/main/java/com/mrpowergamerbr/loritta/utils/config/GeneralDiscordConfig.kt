package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneralDiscordConfig(
		val discord: DiscordConfig,
		@SerialName("shard-controller")
		val shardController: ShardControllerConfig,
		@SerialName("ok-http")
		val okHttp: JdaOkHttpConfig,
		@SerialName("discord-bots")
		val discordBots: DiscordBotsConfig,
		@SerialName("discord-bot-list")
		val discordBotList: DiscordBotListConfig,
		@SerialName("anti-raid-ids")
		val antiRaidIds: List<String>,
		@SerialName("message-encryption")
		val messageEncryption: MessageEncryptionConfig
)