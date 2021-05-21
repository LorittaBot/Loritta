package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

@Serializable
data class DiscordConfig(
		@SerialName("client-token")
		val clientToken: String,
		@SerialName("client-id")
		val clientId: String,
		@SerialName("client-secret")
		val clientSecret: String,
		@SerialName("max-shards")
		val maxShards: Int,
		@SerialName("max-requests-per-host")
		val maxRequestsPerHost: Int,
		val status: OnlineStatus,
		@SerialName("disallow-bots")
		val disallowBots: Boolean,
		@SerialName("bot-whitelist")
		val botWhitelist: List<Long>,
		val intents: List<GatewayIntent>,
		@SerialName("cache-flags")
		val cacheFlags: List<CacheFlag>,
		@SerialName("fan-art-extravaganza")
		val fanArtExtravaganza: FanArtExtravaganzaConfig,
		val delayBetweenActivities: Long? = 60000L,
		val activities: List<LorittaGameStatus>,
		@SerialName("request-limiter")
		val requestLimiter: RequestLimiterConfig
) {
	@Serializable
	data class FanArtExtravaganzaConfig(
		val enabled: Boolean,
		@SerialName("day-of-the-week")
		val dayOfTheWeek: Int,
		@SerialName("fan-arts")
		val fanArts: List<GeneralConfig.LorittaAvatarFanArt>
	)
	@Serializable
	data class LorittaGameStatus(
		val name: String,
		val type: String
	)
	@Serializable
	data class RequestLimiterConfig(
		val enabled: Boolean,
		@SerialName("max-requests-per10-minutes")
		val maxRequestsPer10Minutes: Int,
		@SerialName("console-warn-cooldown")
		val consoleWarnCooldown: Long,
		@SerialName("remove-pending-requests-cooldown")
		val removePendingRequestsCooldown: Long
	)
}