package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class MercadoPagoConfig @JsonCreator constructor(
        val clientId: String,
        val clientSecret: String,
        val ipnAccessToken: String
)