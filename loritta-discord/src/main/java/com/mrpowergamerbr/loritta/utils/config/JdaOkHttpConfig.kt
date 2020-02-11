package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class JdaOkHttpConfig @JsonCreator constructor(
		val readTimeout: Long,
		val connectTimeout: Long,
		val writeTimeout: Long
)