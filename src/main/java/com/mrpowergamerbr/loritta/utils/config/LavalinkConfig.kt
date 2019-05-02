package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

class LavalinkConfig(
        @JsonProperty("enabled")
        val enabled: Boolean,
        @JsonProperty("nodes")
        val nodes: List<LavalinkNode>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    class LavalinkNode(
            @JsonProperty("name")
            val name: String,
            @JsonProperty("address")
            val address: String,
            @JsonProperty("password")
            val password: String
    )
}