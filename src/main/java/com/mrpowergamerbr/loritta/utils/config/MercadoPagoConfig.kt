package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class MercadoPagoConfig(
        @JsonProperty("client-id")
        val clientId: String,
        @JsonProperty("client-secret")
        val clientSecret: String,
        @JsonProperty("ipn-access-token")
        val ipnAccessToken: String
)