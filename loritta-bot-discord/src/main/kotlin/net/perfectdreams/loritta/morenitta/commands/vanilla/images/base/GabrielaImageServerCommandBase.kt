package net.perfectdreams.loritta.morenitta.commands.vanilla.images.base

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import java.util.*

/**
 * Gets a [JsonObject] with the image data (image URL, base64, etc) from the current context.
 *
 * This is multiplatform because Loritta generates a image with a text if there isn't any matching images if the user
 * inputted arguments into the command. This can be true multiplatform without this method after that method is migrated
 * to support multiplatform1
 *
 * @param argument the position of the image in the command
 * @return         the image data, can be null if none was matched
 */
suspend fun CommandContext.imageData(argument: Int): JsonObject? {
    val url = imageUrl(argument, 0)
    val castedLoritta = loritta

    if (url != null && castedLoritta.connectionManager.isTrusted(url))
        return buildJsonObject {
            put("type", "url")
            put("content", url)
        }

    if (args.isNotEmpty()) {
        val theTextThatWillBeWritten = args.drop(argument).joinToString(" ")
        if (theTextThatWillBeWritten.isNotEmpty()) {
            val textAsImage = JVMImage(ImageUtils.createTextAsImage(loritta, 256, 256, theTextThatWillBeWritten))

            return buildJsonObject {
                put("type", "base64")
                put("content", Base64.getEncoder().encodeToString(textAsImage.toByteArray()))
            }
        }
    } else {
        val urlSearchingMessages = imageUrl(argument)
        if (urlSearchingMessages != null && castedLoritta.connectionManager.isTrusted(urlSearchingMessages))
            return buildJsonObject {
                put("type", "url")
                put("content", urlSearchingMessages)
            }
    }

    return null
}