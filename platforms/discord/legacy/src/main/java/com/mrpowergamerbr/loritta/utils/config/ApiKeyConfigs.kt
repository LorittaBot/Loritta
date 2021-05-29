package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherMapConfig(
        val apiKey: String
)

@Serializable
data class GoogleVisionKey(
        val apiKey: String
)

@Serializable
data class GoogleRecaptcha(
        val serverVoteToken: String,
        val reputationToken: String
)

@Serializable
data class DiscordBotsConfig(
        val enabled: Boolean,
        val apiKey: String
)

@Serializable
data class DiscordBotListConfig(
        val enabled: Boolean,
        val apiKey: String
)

@Serializable
data class TwitchConfig(
        val clientId: String,
		val clientSecret: String
)

@Serializable
data class GeneralWebhookConfig(
        val webhookSecret: String
)