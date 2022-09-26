package net.perfectdreams.loritta.morenitta.commands.vanilla.images.base

import net.perfectdreams.loritta.morenitta.Loritta
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.api.commands.Command
import net.perfectdreams.loritta.common.api.commands.CommandContext
import net.perfectdreams.loritta.common.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.util.*

/**
 * Commands that use Gabriela's Image Server Generator tool should use this class!
 *
 * TODO: This class could be multiplatform if the Base64 implementation was multiplatform!
 *
 * @param endpoint the page endpoint (example: "/api/v1/videos/carly-aaah")
 * @param fileName the sent file file name (example: "carly_aaah.mp4")
 */
abstract class GabrielaImageServerCommandBase(
    loritta: LorittaBot,
    labels: List<String>,
    val imageCount: Int,
    val descriptionKey: String,
    val endpoint: String,
    val fileName: String,
    val examplesKey: String? = null,
    category: net.perfectdreams.loritta.common.commands.CommandCategory = net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES,
    val slashCommandName: String? = null
) : LorittaAbstractCommandBase(
    loritta,
    labels,
    category
) {
    override fun command() = create {
        localizedDescription(descriptionKey)

        val examplesKey = when (imageCount) {
            1 -> Command.SINGLE_IMAGE_EXAMPLES_KEY
            2 -> Command.TWO_IMAGES_EXAMPLES_KEY
            else -> if (examplesKey != null) LocaleKeyData(examplesKey) else null
        }

        if (examplesKey != null)
            localizedExamples(examplesKey)

        executes {
            slashCommandName?.let {
                OutdatedCommandUtils.sendOutdatedCommandMessage(
                    this,
                    locale,
                    it
                )
            }

            val imagesData = (0 until imageCount).map {
                imageData(it) ?: run {
                    if (args.isEmpty())
                        explainAndExit()
                    else
                        fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
                }
            }

            val response = loritta.httpWithoutTimeout.post("https://gabriela.loritta.website$endpoint") {
                setBody(
                    buildJsonObject {
                        putJsonArray("images") {
                            for (data in imagesData)
                                add(data)
                        }
                    }.toString()
                )
            }

            // If the status code is between 400.499, then it means that it was (probably) a invalid input or something
            if (response.status.value in 400..499)
                fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
            else if (response.status.value !in 200..299) // This should show the error message because it means that the server had a unknown error
                fail(locale["commands.errorWhileExecutingCommand", Emotes.LORI_RAGE, Emotes.LORI_CRYING], "\uD83E\uDD37")

            sendFile(response.readBytes(), fileName)
        }
    }
}

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