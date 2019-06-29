package net.perfectdreams.loritta.website.utils

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

fun TemmieDiscordAuth.serialize(): String {
    return Constants.JSON_MAPPER.writeValueAsString(
        JsonNodeFactory.instance.objectNode()
            .put("accessToken", this.accessToken)
            .put("refreshToken", this.refreshToken)
            .put("expiresIn", this.expiresIn)
            .put("generatedAt", this.generatedAt)
    )
}

fun String.deserialize(): TemmieDiscordAuth {
    val asJson = Constants.JSON_MAPPER.readTree(this)
    return TemmieDiscordAuth(
            LorittaWebsite.INSTANCE.config.clientId,
            LorittaWebsite.INSTANCE.config.clientToken,
        null,
        "https://spicy.loritta.website/auth",
        listOf("identify", "email", "connections", "guilds"),
        asJson.get("accessToken").textValue(),
        asJson.get("refreshToken").textValue(),
        asJson.get("expiresIn").longValue(),
        asJson.get("generatedAt").longValue()
    )
}