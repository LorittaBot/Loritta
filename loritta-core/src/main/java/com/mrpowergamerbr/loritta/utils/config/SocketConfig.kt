package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class SocketConfig @JsonCreator constructor(
        val enabled: Boolean,
        val shardId: Int,
        val clientName: String,
        val port: Int
)