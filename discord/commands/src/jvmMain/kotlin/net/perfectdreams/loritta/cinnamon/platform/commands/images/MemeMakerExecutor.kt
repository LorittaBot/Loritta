package net.perfectdreams.loritta.cinnamon.platform.commands.images

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.MemeMakerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.executeAndHandleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class MemeMakerExecutor(val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(MemeMakerExecutor::class) {
        object Options : CommandOptions() {
            val imageReference = imageReference("image", I18nKeysData.Commands.Category.Images.Options.Image)
                .register()

            val line1 = string("line1", MemeMakerCommand.I18N_PREFIX.Options.Line1)
                .register()

            val line2 = optionalString("line2", MemeMakerCommand.I18N_PREFIX.Options.Line2)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[options.imageReference]
        val line1 = args[options.line1]
        val line2 = args[options.line2]

        val result = client.executeAndHandleExceptions(
            context,
                    "/api/v1/images/meme-maker",
            buildJsonObject {
                putJsonArray("images") {
                    addJsonObject {
                        put("type", "url")
                        put("content", imageReference.url)
                    }
                }

                putJsonArray("strings") {
                    addJsonObject {
                        put("string", line1)
                    }

                    if (line2 != null) {
                        addJsonObject {
                            put("string", line2)
                        }
                    }
                }
            }
        )

        context.sendMessage {
            addFile("meme_maker.png", result.inputStream())
        }
    }
}