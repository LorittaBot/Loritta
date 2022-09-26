package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.ChavesOpeningRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.gabrielaimageserver.exceptions.InvalidChavesOpeningTextException
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations.ChavesCommand

class ChavesOpeningExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        // The description is replaced with "User, URL or Emote" so we don't really care that we are using "TodoFixThisData" here
        val chiquinhaImage = imageReference("chiquinha")

        val girafalesImage = imageReference("girafales")

        val bruxaImage = imageReference("bruxa")

        val quicoImage = imageReference("quico")

        val florindaImage = imageReference("florinda")

        val madrugaImage = imageReference("madruga")

        val barrigaImage = imageReference("barriga")

        val chavesImage = imageReference("chaves")

        val showName = string("show_name", ChavesCommand.I18N_PREFIX.Opening.Options.ShowName.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val chiquinha = args[options.chiquinhaImage].get(context)
        val girafales = args[options.girafalesImage].get(context)
        val bruxa = args[options.bruxaImage].get(context)
        val quico = args[options.quicoImage].get(context)
        val florinda = args[options.florindaImage].get(context)
        val madruga = args[options.madrugaImage].get(context)
        val barriga = args[options.barrigaImage].get(context)
        val chaves = args[options.chavesImage].get(context)
        val showName = args[options.showName]

        val result = try {
            client.handleExceptions(context) {
                client.videos.chavesOpening(
                    ChavesOpeningRequest(
                        URLImageData(
                            chiquinha.url
                        ),
                        URLImageData(
                            girafales.url
                        ),
                        URLImageData(
                            bruxa.url
                        ),
                        URLImageData(
                            quico.url
                        ),
                        URLImageData(
                            florinda.url
                        ),
                        URLImageData(
                            madruga.url
                        ),
                        URLImageData(
                            barriga.url
                        ),
                        URLImageData(
                            chaves.url
                        ),
                        showName
                    )
                )
            }
        } catch (e: InvalidChavesOpeningTextException) {
            context.fail(
                context.i18nContext.get(ChavesCommand.I18N_PREFIX.Opening.InvalidShowName),
                Emotes.LoriSob
            )
        }

        context.sendMessage {
            addFile("chaves_opening.mp4", result.inputStream())
        }
    }
}