package net.perfectdreams.loritta.website.utils

import com.fasterxml.jackson.databind.JsonNode

class GuildConfig(
    val commandPrefix: String
) {
    companion object {
        fun from(node: JsonNode): GuildConfig {
            return GuildConfig(
                node["commandPrefix"].textValue()
            )
        }
    }
}