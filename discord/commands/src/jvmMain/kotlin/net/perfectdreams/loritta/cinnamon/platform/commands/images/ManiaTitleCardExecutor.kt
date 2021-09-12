package net.perfectdreams.loritta.cinnamon.platform.commands.images

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SonicCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.executeAndHandleExceptions
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class ManiaTitleCardExecutor(val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(ManiaTitleCardExecutor::class) {
        object Options : CommandOptions() {
            val line1 = string("line1", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)
                .register()

            val line2 = optionalString("line2", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val line1 = args[options.line1]
        val line2 = args[options.line2]

        val result = client.executeAndHandleExceptions(
            context,
                    "/api/v1/images/mania-title-card",
            buildJsonObject {
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
            addFile("mania_title_card.png", result.inputStream())
        }
    }
}