package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class FortniteApiConfig @JsonCreator constructor(
        @JsonProperty("token")
        val token: String,
        @JsonProperty("creator-code")
        val creatorCode: String
)