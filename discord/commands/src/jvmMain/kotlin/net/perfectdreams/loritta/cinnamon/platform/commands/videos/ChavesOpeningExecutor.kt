package net.perfectdreams.loritta.cinnamon.platform.commands.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.ChavesOpeningRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.gabrielaimageserver.exceptions.InvalidChavesOpeningTextException
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.ChavesCommand

class ChavesOpeningExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            // The description is replaced with "User, URL or Emote" so we don't really care that we are using "TodoFixThisData" here
            val chiquinhaImage = imageReference("chiquinha", TodoFixThisData)
                .register()
            val girafalesImage = imageReference("girafales", TodoFixThisData)
                .register()
            val bruxaImage = imageReference("bruxa", TodoFixThisData)
                .register()
            val quicoImage = imageReference("quico", TodoFixThisData)
                .register()
            val florindaImage = imageReference("florinda", TodoFixThisData)
                .register()
            val madrugaImage = imageReference("madruga", TodoFixThisData)
                .register()
            val barrigaImage = imageReference("barriga", TodoFixThisData)
                .register()
            val chavesImage = imageReference("chaves", TodoFixThisData)
                .register()
            val showName = string("show_name", ChavesCommand.I18N_PREFIX.Opening.Options.ShowName.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val chiquinha = args[options.chiquinhaImage]
        val girafales = args[options.girafalesImage]
        val bruxa = args[options.bruxaImage]
        val quico = args[options.quicoImage]
        val florinda = args[options.florindaImage]
        val madruga = args[options.madrugaImage]
        val barriga = args[options.barrigaImage]
        val chaves = args[options.chavesImage]
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