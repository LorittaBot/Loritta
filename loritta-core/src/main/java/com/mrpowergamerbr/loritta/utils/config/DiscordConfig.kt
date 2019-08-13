package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import net.dv8tion.jda.api.OnlineStatus

class DiscordConfig @JsonCreator constructor(
        val clientToken: String,
        val clientId: String,
        val clientSecret: String,
        var minShardId: Int,
        var maxShardId: Int,
        val maxShards: Int,
        val maxRequestsPerHost: Int,
        val status: OnlineStatus,
        val addBotUrl: String,
        val authorizationUrl: String,
        val disallowBots: Boolean,
        val botWhitelist: List<Long>,
        val fanArtExtravaganza: FanArtExtravaganzaConfig,
        val requestLimiter: RequestLimiterConfig,
        val activities: List<LorittaGameStatus>
) {
    class FanArtExtravaganzaConfig @JsonCreator constructor(
            val enabled: Boolean,
            val dayOfTheWeek: Int,
            val fanArts: List<GeneralConfig.LorittaAvatarFanArt>
    )
    class LorittaGameStatus @JsonCreator constructor(
            val name: String,
            val type: String
    )
    class RequestLimiterConfig @JsonCreator constructor(
            val enabled: Boolean,
            val maxRequests: Int,
            val ignoreRequestsFor: Int,
            val allowMessagesWith: Array<String>
    )
}