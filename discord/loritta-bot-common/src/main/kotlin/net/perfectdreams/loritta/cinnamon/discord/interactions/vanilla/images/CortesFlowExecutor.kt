package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.CortesFlowRequest
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.utils.text.TextUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class CortesFlowExecutor(
    loritta: LorittaBot,
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