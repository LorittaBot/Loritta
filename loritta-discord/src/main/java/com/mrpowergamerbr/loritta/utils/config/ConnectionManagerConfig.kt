package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionManagerConfig(
        val trustedDomains: List<String>,
        val blockedDomains: List<String>
)