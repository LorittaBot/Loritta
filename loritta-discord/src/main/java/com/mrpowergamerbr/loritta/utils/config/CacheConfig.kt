package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class CacheConfig @JsonCreator constructor(
		val serverConfigs: ServerConfigCacheConfig
) {
	class ServerConfigCacheConfig @JsonCreator constructor(
			val maximumSize: Long,
			val expireAfterWrite: Long
	)
}