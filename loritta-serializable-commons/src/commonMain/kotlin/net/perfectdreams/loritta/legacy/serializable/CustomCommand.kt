package net.perfectdreams.loritta.legacy.serializable

import kotlinx.serialization.Serializable

@Serializable
class CustomCommand(
		val label: String,
		val codeType: CustomCommandCodeType,
		val code: String
)