package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class SocketConfig(
        @JsonProperty("enabled")
        val enabled: Boolean,
        @JsonProperty("port")
        val port: Int
)