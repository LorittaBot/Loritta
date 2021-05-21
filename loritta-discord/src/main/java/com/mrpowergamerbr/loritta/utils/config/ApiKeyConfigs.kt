package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherMapConfig(
        @SerialName("api-key")
        val apiKey: String
)

@Serializable
data class GoogleVisionKey(
        @SerialName("api-key")
        val apiKey: String
)

@Serializable
data class GoogleRecaptcha(
        @SerialName("server-vote-token")
        val serverVoteToken: String,
        @SerialName("reputation-token")
        val reputationToken: String
)

@Serializable
data class DiscordBotsConfig(
        val enabled: Boolean,
        @SerialName("api-key")
        val apiKey: String
)

@Serializable
data class DiscordBotListConfig(
        val enabled: Boolean,
        @SerialName("api-key")
        val apiKey: String
)

@Serializable
data class TwitchConfig(
        @SerialName("client-id")
        val clientId: String,
        @SerialName("client-secret")
		val clientSecret: String
)

@Serializable
data class GeneralWebhookConfig(
        @SerialName("webhook-secret")
        val webhookSecret: String
)