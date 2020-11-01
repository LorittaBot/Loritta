package net.perfectdreams.loritta.plugin.rosbife.commands.base

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.api.commands.CommandContext

actual suspend fun CommandContext.imageData(argument: Int): JsonObject? {
    val url = imageUrl(argument)

    if (url != null)
        return buildJsonObject {
            put("type", "url")
            put("content", url)
        }

    return null
}