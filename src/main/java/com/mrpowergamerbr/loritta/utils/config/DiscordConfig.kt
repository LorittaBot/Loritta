package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import net.dv8tion.jda.api.OnlineStatus

class DiscordConfig @JsonCreator constructor(
        val clientToken: String,
        val clientId: String,
        val clientSecret: String,
        val shards: Int,
        val status: OnlineStatus,
        val addBotUrl: String,
        val authorizationUrl: String,
        val fanArtExtravaganza: FanArtExtravaganzaConfig,
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
}