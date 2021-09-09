package net.perfectdreams.loritta.cinnamon.commands.images

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.common.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.common.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.common.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.executeAndHandleExceptions
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils

class CortesFlowExecutor(val emotes: Emotes, val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CortesFlowExecutor::class) {
        object Options : CommandOptions() {
            val type = string("thumbnail", BRMemesCommand.I18N_PREFIX.Cortesflow.Options.Thumbnail)
                .also { option ->
                    BRMemesCommand.cortesFlowThumbnails.forEach {
                        option.choice(
                            it,
                            StringI18nData(
                                StringI18nKey("${BRMemesCommand.I18N_CORTESFLOW_KEY_PREFIX}.thumbnails.${TextUtils.kebabToLowerCamelCase(it)}"),
                                emptyMap()
                            )
                        )
                    }
                }
                .register()

            val text = string("text", BRMemesCommand.I18N_PREFIX.Cortesflow.Options.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

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