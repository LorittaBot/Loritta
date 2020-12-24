package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class OpenWeatherMapConfig @JsonCreator constructor(
        val apiKey: String
)

class GoogleVisionKey @JsonCreator constructor(
        val apiKey: String
)

class GoogleRecaptcha @JsonCreator constructor(
        val serverVoteToken: String,
        val reputationToken: String
)

class DiscordBotsConfig @JsonCreator constructor(
        val enabled: Boolean,
        val apiKey: String
)

class DiscordBotListConfig@JsonCreator constructor(
        val enabled: Boolean,
        val apiKey: String
)

class TwitchConfig @JsonCreator constructor(
        val clientId: String,
		val clientSecret: String
)

class GeneralWebhookConfig @JsonCreator constructor(
        val webhookSecret: String
)