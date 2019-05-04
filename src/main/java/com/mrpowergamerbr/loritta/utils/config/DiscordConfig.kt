package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty
import net.dv8tion.jda.api.OnlineStatus

class DiscordConfig(
        @JsonProperty("client-token")
        val clientToken: String,
        @JsonProperty("client-id")
        val clientId: String,
        @JsonProperty("client-secret")
        val clientSecret: String,
        @JsonProperty("shards")
        val shards: Int,
        @JsonProperty("status")
        val status: OnlineStatus,
        @JsonProperty("add-bot-url")
        val addBotUrl: String,
        @JsonProperty("authorization-url")
        val authorizationUrl: String,
        @JsonProperty("fan-art-extravaganza")
        val fanArtExtravaganza: FanArtExtravaganzaConfig,
        @JsonProperty("activities")
        val activities: List<LorittaGameStatus>
) {
    class FanArtExtravaganzaConfig(
            @JsonProperty("enabled")
            val enabled: Boolean,
            @JsonProperty("day-of-the-week")
            val dayOfTheWeek: Int,
            @JsonProperty("fan-arts")
            val fanArts: List<LorittaConfig.LorittaAvatarFanArt>
    )
    class LorittaGameStatus(
            @JsonProperty("name")
            val name: String,
            @JsonProperty("type")
            val type: String
    )
}