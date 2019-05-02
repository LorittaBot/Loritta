package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class MashapeConfig(
        @JsonProperty("api-key")
        val apiKey: String
)

class OpenWeatherMapConfig(
        @JsonProperty("api-key")
        val apiKey: String
)

class GoogleVisionKey(
        @JsonProperty("api-key")
        val apiKey: String
)

class GoogleRecaptcha(
        @JsonProperty("server-vote-token")
        val serverVoteToken: String,
        @JsonProperty("reputation-token")
        val reputationToken: String
)

class GitHubConfig(
        @JsonProperty("api-key")
        val apiKey: String,
        @JsonProperty("repository-url")
        val repositoryUrl: String
)

class DiscordBotsConfig(
        @JsonProperty("enabled")
        val enabled: Boolean,
        @JsonProperty("api-key")
        val apiKey: String
)

class DiscordBotListConfig(
        @JsonProperty("enabled")
        val enabled: Boolean,
        @JsonProperty("api-key")
        val apiKey: String
)

class TwitchConfig(
        @JsonProperty("client-id")
        val clientId: String
)

class MixerConfig(
        @JsonProperty("client-id")
        val clientId: String,
        @JsonProperty("client-secret")
        val clientSecret: String,
        @JsonProperty("webhook-secret")
        val webhookSecret: String
)