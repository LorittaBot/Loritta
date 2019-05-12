package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class SocketConfig @JsonCreator constructor(
        val enabled: Boolean,
        val port: Int
)