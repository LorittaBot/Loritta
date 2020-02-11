package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class ConnectionManagerConfig @JsonCreator constructor(
        val proxyUntrustedConnections: Boolean,
        val proxySources: List<String>,
        val proxies: List<String>,
        val trustedDomains: List<String>,
        val blockedDomains: List<String>
)