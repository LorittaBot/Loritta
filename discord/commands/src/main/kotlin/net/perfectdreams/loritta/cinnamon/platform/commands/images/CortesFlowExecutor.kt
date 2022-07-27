package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.CortesFlowRequest
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class CortesFlowExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val type = string("thumbnail", BRMemesCommand.I18N_PREFIX.Cortesflow.Options.Thumbnail) {
            BRMemesCommand.cortesFlowThumbnails.forEach {
                choice(
                    StringI18nData(
                        StringI18nKey(
                            "${BRMemesCommand.I18N_CORTESFLOW_KEY_PREFIX}.thumbnails.${
                                TextUtils.kebabToLowerCamelCase(
                                    it
                                )
                            }"
                        ),
                        emptyMap()
                    ),
                    it
                )
            }
        }

        val text = string("text", BRMemesCommand.I18N_PREFIX.Cortesflow.Options.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val type = args[options.type]
        val text = args[options.text]

        val result = client.handleExceptions(context) {
            client.images.cortesFlow(type, CortesFlowRequest(text))
        }

        context.sendMessage {
            addFile("cortes_flow.jpg", result.inputStream())
        }
    }
}