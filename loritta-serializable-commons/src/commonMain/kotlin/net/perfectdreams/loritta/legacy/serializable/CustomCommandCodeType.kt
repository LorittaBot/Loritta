package net.perfectdreams.loritta.legacy.serializable

import kotlinx.serialization.Serializable

@Serializable
enum class CustomCommandCodeType {
	UNKNOWN,
	JAVASCRIPT,
	KOTLIN,
	SIMPLE_TEXT
}