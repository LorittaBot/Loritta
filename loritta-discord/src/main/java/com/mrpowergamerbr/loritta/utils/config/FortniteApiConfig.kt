package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FortniteApiConfig(
        @SerialName("token")
        val token: String,
        @SerialName("creator-code")
        val creatorCode: String
)