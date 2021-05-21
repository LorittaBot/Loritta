package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class CacheConfig(
		val serverConfigs: ServerConfigCacheConfig
) {
	@Serializable
	data class ServerConfigCacheConfig(
			val maximumSize: Long,
			val expireAfterWrite: Long
	)
}