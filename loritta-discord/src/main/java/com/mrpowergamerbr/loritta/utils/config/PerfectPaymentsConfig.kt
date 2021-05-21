package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PerfectPaymentsConfig(
	val url: String,
	val notificationToken: String,
	val token: String
)