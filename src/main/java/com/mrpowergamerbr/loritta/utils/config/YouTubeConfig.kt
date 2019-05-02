package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class YouTubeConfig(
        @JsonProperty("api-keys")
        val apiKeys: List<String>
)