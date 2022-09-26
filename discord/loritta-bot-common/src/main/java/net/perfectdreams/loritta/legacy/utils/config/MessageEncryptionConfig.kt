package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class MessageEncryptionConfig(
		val encryptionKey: String
)