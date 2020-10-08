package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class PerfectPaymentsConfig @JsonCreator constructor(
		@JsonProperty("notification-token")
		val notificationToken: String,
		val token: String
)