package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

class DiscordConfig @JsonCreator constructor(
		val clientToken: String,
		val clientId: String,
		val clientSecret: String,
		val maxShards: Int,
		val maxRequestsPerHost: Int,
		val status: OnlineStatus,
		val disallowBots: Boolean,
		val botWhitelist: List<Long>,
		val intents: List<GatewayIntent>,
		val cacheFlags: List<CacheFlag>,
		val fanArtExtravaganza: FanArtExtravaganzaConfig,
		val delayBetweenActivities: Long,
		val activities: List<LorittaGameStatus>,
		val requestLimiter: RequestLimiterConfig
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
			val maxRequestsPer10Minutes: Int,
			val consoleWarnCooldown: Long,
			val removePendingRequestsCooldown: Long
	)
}