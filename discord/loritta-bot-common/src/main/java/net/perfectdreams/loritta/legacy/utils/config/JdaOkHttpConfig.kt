package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class JdaOkHttpConfig(
		val readTimeout: Long,
		val connectTimeout: Long,
		val writeTimeout: Long,
		val proxyUrl: String? = null
)