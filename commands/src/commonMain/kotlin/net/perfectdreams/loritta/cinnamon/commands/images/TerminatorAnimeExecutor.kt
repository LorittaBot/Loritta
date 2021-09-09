package net.perfectdreams.loritta.cinnamon.commands.images

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.cinnamon.common.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.common.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.common.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.executeAndHandleExceptions

class TerminatorAnimeExecutor(val emotes: Emotes, val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TerminatorAnimeExecutor::class) {
        object Options : CommandOptions() {
            val line1 = string("terminator", TerminatorAnimeCommand.I18N_PREFIX.Options.TextTerminator)
                .register()

            val line2 = optionalString("girl", TerminatorAnimeCommand.I18N_PREFIX.Options.TextGirl)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val line1 = args[options.line1]
        val line2 = args[options.line2]

        val result = client.executeAndHandleExceptions(
            context,
            emotes,
            "/api/v1/images/terminator-anime",
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
            addFile("terminator_anime.png", result)
        }
    }
}