package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JdaOkHttpConfig(
		@SerialName("read-timeout")
		val readTimeout: Long,
		@SerialName("connect-timeout")
		val connectTimeout: Long,
		@SerialName("write-timeout")
		val writeTimeout: Long
)