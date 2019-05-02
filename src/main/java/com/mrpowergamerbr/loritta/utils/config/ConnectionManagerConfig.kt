package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class ConnectionManagerConfig(
        @JsonProperty("proxy-untrusted-connections")
        val proxyUntrustedConnections: Boolean,
        @JsonProperty("proxy-sources")
        val proxySources: List<String>,
        @JsonProperty("proxies")
        val proxies: List<String>,
        @JsonProperty("trusted-domains")
        val trustedDomains: List<String>,
        @JsonProperty("blocked-domains")
        val blockedDomains: List<String>
)