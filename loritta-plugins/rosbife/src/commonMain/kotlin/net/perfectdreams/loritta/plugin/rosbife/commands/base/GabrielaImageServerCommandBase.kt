package net.perfectdreams.loritta.plugin.rosbife.commands.base

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.utils.Emotes

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
        val sendTypingStatus: Boolean = false,
        val examplesKey: String? = null,
        category: CommandCategory = CommandCategory.IMAGES
) : LorittaAbstractCommandBase(
        loritta,
        labels,
        category
) {
    override fun command() = create {
        localizedDescription(descriptionKey)
        this.sendTypingStatus = this@GabrielaImageServerCommandBase.sendTypingStatus

        val examplesKey = when (imageCount) {
            1 -> Command.SINGLE_IMAGE_EXAMPLES_KEY
            2 -> Command.TWO_IMAGES_EXAMPLES_KEY
            else -> if (examplesKey != null) LocaleKeyData(examplesKey) else null
        }

        if (examplesKey != null)
            localizedExamples(examplesKey)

        executes {
            val imagesData = (0 until imageCount).map {
                imageData(it) ?: run {
                    if (args.isEmpty())
                        explainAndExit()
                    else
                        fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
                }
            }

            val response = loritta.httpWithoutTimeout.post<HttpResponse>("https://gabriela.loritta.website$endpoint") {
                body = buildJsonObject {
                    putJsonArray("images") {
                        for (data in imagesData)
                            add(data)
                    }
                }.toString()
            }

            // If the status code is between 400.499, then it means that it was (probably) a invalid input or something
            if (response.status.value in 400..499)
                fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
            else if (response.status.value !in 200..299) // This should show the error message because it means that the server had a unknown error
                fail(locale["commands.errorWhileExecutingCommand", Emotes.LORI_RAGE, Emotes.LORI_CRYING], "\uD83E\uDD37")

            sendFile(response.receive(), fileName)
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
expect suspend fun CommandContext.imageData(argument: Int): JsonObject?