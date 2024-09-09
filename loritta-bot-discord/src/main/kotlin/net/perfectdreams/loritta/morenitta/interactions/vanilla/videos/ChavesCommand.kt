package net.perfectdreams.loritta.morenitta.interactions.vanilla.videos

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.ChavesOpeningRequest
import net.perfectdreams.gabrielaimageserver.data.CocieloChavesRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.gabrielaimageserver.exceptions.InvalidChavesOpeningTextException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.util.*

class ChavesCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Chaves
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.VIDEOS, UUID.fromString("cfe18402-8b48-4f3e-a976-4481e221580b")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        // enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Cocielo.Label, I18N_PREFIX.Cocielo.Description, UUID.fromString("d7fb8862-3e58-4b61-acdf-18b7aef5d237")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("chavescocielo")
                add("cocielochaves")
            }

            executor = ChavesCocieloExecutor(client)
        }

        subcommand(I18N_PREFIX.Opening.Label, I18N_PREFIX.Opening.Description, UUID.fromString("22ab7bfd-da16-4617-8d3e-9a0d326384bc")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("chavesabertura")
                add("aberturachaves")
            }

            executor = ChavesOpeningExecutor(client)
        }
    }

    class ChavesCocieloExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor() {
        inner class CommandOptions : ApplicationCommandOptions() {
            // The description is replaced with "User, URL or Emote" so we don't really care that we are using "TodoFixThisData" here
            val friend1Image = imageReference("friend1")

            val friend2Image = imageReference("friend2")

            val friend3Image = imageReference("friend3")

            val friend4Image = imageReference("friend4")

            val friend5Image = imageReference("friend5")
        }

        override val options = CommandOptions()

        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            context.deferChannelMessage(false) // Defer message because image manipulation is kinda heavy

            val friend1 = args[options.friend1Image].get(context)
            val friend2 = args[options.friend2Image].get(context)
            val friend3 = args[options.friend3Image].get(context)
            val friend4 = args[options.friend4Image].get(context)
            val friend5 = args[options.friend5Image].get(context)

            val result = client.handleExceptions(context) {
                client.videos.cocieloChaves(
                    CocieloChavesRequest(
                        URLImageData(
                            friend1
                        ),
                        URLImageData(
                            friend2
                        ),
                        URLImageData(
                            friend3
                        ),
                        URLImageData(
                            friend4
                        ),
                        URLImageData(
                            friend5
                        )
                    )
                )
            }

            context.reply(false) {
                files += FileUpload.fromData(result.inputStream(), "cocielo_chaves.mp4")
            }
        }
    }

    class ChavesOpeningExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor() {
        inner class CommandOptions : ApplicationCommandOptions() {
            // The description is replaced with "User, URL or Emote" so we don't really care that we are using "TodoFixThisData" here
            val chiquinhaImage = imageReference("chiquinha")

            val girafalesImage = imageReference("girafales")

            val bruxaImage = imageReference("bruxa")

            val quicoImage = imageReference("quico")

            val florindaImage = imageReference("florinda")

            val madrugaImage = imageReference("madruga")

            val barrigaImage = imageReference("barriga")

            val chavesImage = imageReference("chaves")

            val showName = string("show_name", I18N_PREFIX.Opening.Options.ShowName.Text)
        }

        override val options = CommandOptions()

        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            context.deferChannelMessage(false) // Defer message because image manipulation is kinda heavy

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
                                chiquinha
                            ),
                            URLImageData(
                                girafales
                            ),
                            URLImageData(
                                bruxa
                            ),
                            URLImageData(
                                quico
                            ),
                            URLImageData(
                                florinda
                            ),
                            URLImageData(
                                madruga
                            ),
                            URLImageData(
                                barriga
                            ),
                            URLImageData(
                                chaves
                            ),
                            showName
                        )
                    )
                }
            } catch (e: InvalidChavesOpeningTextException) {
                context.fail(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Opening.InvalidShowName),
                        Emotes.LoriSob
                    )
                }
            }

            context.reply(false) {
                files += FileUpload.fromData(result.inputStream(), "chaves_opening.mp4")
            }
        }
    }
}