package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
enum class CustomCommandCodeType {
	UNKNOWN,
	JAVASCRIPT,
	KOTLIN,
	SIMPLE_TEXT
}