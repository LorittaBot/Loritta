package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

@Serializable
data class DiscordConfig(
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
		val activity: LorittaGameStatus,
		val requestLimiter: RequestLimiterConfig
) {
	@Serializable
	data class FanArtExtravaganzaConfig(
		val enabled: Boolean,
		val dayOfTheWeek: Int,
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
		val maxRequestsPer10Minutes: Int,
		val consoleWarnCooldown: Long,
		val removePendingRequestsCooldown: Long
	)
}