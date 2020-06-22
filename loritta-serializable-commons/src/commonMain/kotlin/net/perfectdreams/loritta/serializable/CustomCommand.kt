package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
class CustomCommand(
		val label: String,
		val code: String
)