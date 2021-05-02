package net.perfectdreams.loritta.commands.images

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.commands.images.declarations.CortesFlowCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.executeAndHandleExceptions

class CortesFlowExecutor(val emotes: Emotes, val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CortesFlowExecutor::class) {
        object Options : CommandOptions() {
            val type = string("thumbnail", LocaleKeyData("${CortesFlowCommand.LOCALE_PREFIX}.selectThumbnail"))
                .also { option ->
                    CortesFlowCommand.lists.forEach {
                        option.choice(it, LocaleKeyData(it))
                    }
                }
                .register()

            val text = string("text", LocaleKeyData("${CortesFlowCommand.LOCALE_PREFIX}.selectText"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val type = args[options.type]
        val text = args[options.text]

        val result = client.executeAndHandleExceptions(
            context,
            emotes,
            "/api/v1/images/cortes-flow/$type",
            buildJsonObject {
                putJsonArray("strings") {
                    addJsonObject {
                        put("string", text)
                    }
                }
            }
        )

        context.sendMessage {
            addFile("cortes_flow.jpg", result)
        }
    }
}