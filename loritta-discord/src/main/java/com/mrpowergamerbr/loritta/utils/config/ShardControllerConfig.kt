package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class ShardControllerConfig @JsonCreator constructor(
		val enabled: Boolean,
		val url: String,
		val buckets: Int
)