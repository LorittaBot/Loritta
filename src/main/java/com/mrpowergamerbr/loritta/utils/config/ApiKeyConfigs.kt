package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class MashapeConfig @JsonCreator constructor(
        val apiKey: String
)

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

class GitHubConfig @JsonCreator constructor(
        val apiKey: String,
        val repositoryUrl: String
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
        val clientId: String
)

class MixerConfig @JsonCreator constructor(
        val clientId: String,
        val clientSecret: String,
        val webhookSecret: String
)