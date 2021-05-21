package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerfectPaymentsConfig(
	val url: String,
	@SerialName("notification-token")
	val notificationToken: String,
	val token: String
)