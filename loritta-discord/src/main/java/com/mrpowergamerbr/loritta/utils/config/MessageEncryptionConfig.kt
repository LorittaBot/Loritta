package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class MessageEncryptionConfig @JsonCreator constructor(
		val encryptionKey: String
)