package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageEncryptionConfig(
		@SerialName("encryption-key")
		val encryptionKey: String
)