package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

data class ParallaxCodeServerConfig @JsonCreator constructor(
		val url: String
)