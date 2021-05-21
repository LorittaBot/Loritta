package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CacheConfig(
		@SerialName("server-configs")
		val serverConfigs: ServerConfigCacheConfig
) {
	@Serializable
	data class ServerConfigCacheConfig(
			@SerialName("maximum-size")
			val maximumSize: Long,
			@SerialName("expire-after-write")
			val expireAfterWrite: Long
	)
}