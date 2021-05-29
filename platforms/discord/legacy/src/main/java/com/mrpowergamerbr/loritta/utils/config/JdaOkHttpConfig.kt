package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class JdaOkHttpConfig(
		val readTimeout: Long,
		val connectTimeout: Long,
		val writeTimeout: Long
)