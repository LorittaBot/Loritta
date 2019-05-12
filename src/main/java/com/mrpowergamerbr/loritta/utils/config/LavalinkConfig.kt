package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class LavalinkConfig @JsonCreator constructor(
        val enabled: Boolean,
        val nodes: List<LavalinkNode>
) {
    class LavalinkNode @JsonCreator constructor(
            val name: String,
            val address: String,
            val password: String
    )
}