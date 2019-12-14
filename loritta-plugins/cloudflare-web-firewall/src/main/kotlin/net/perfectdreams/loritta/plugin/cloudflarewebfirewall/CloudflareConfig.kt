package net.perfectdreams.loritta.plugin.cloudflarewebfirewall

import com.fasterxml.jackson.annotation.JsonCreator

class CloudflareConfig @JsonCreator constructor(
		val enabled: Boolean,
		val queueSize: Int,
		val delayBetweenChecks: Long,
		val minimumIpCountToAllowChecks: Long,
		val requiredConnectionsForIpBlacklist: Long,
		val asnBlacklistRatio: Double,
		val authEmail: String,
		val authKey: String,
		val zoneId: String,
		val ruleId: String,

		val whitelistedIps: List<String>,
		val whitelistedAsns: List<Int>
)