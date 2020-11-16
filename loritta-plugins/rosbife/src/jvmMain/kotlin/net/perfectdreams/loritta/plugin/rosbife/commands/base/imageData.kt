package net.perfectdreams.loritta.plugin.rosbife.commands.base

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.ImageUtils
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.utils.image.JVMImage
import java.util.*

actual suspend fun CommandContext.imageData(argument: Int): JsonObject? {
    val url = imageUrl(argument, 0)
    val castedLoritta = (loritta as Loritta)

    if (url != null && castedLoritta.connectionManager.isTrusted(url))
        return buildJsonObject {
            put("type", "url")
            put("content", url)
        }

    if (args.isNotEmpty()) {
        val theTextThatWillBeWritten = args.drop(argument).joinToString(" ")
        if (theTextThatWillBeWritten.isNotEmpty()) {
            val textAsImage = JVMImage(ImageUtils.createTextAsImage(256, 256, theTextThatWillBeWritten))

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