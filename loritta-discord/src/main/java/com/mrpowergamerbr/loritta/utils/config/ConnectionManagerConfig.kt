package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionManagerConfig(
        @SerialName("trusted-domains")
        val trustedDomains: List<String>,
        @SerialName("blocked-domains")
        val blockedDomains: List<String>
)