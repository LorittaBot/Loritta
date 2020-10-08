package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class ConnectionManagerConfig @JsonCreator constructor(
        val trustedDomains: List<String>,
        val blockedDomains: List<String>
)