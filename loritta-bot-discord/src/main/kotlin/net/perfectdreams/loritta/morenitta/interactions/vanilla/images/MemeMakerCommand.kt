package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.MemeMakerRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachment
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class MemeMakerCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Mememaker
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES, UUID.fromString("23947584-91f0-4876-b81b-e7057a69a280")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("mememaker")
            add("meme")
        }

        executor = MemeMakerExecutor()
    }

    inner class MemeMakerExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val line1 = string("line1", I18N_PREFIX.Options.Line1)
            val line2 = optionalString("line2", I18N_PREFIX.Options.Line2)

            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false ) // Defer message because image manipulation is kinda heavy

            val imageReference = args[options.imageReference].get(context)
            val line1 = args[options.line1]
            val line2 = args[options.line2]

            val result = client.handleExceptions(context) {
                client.images.memeMaker(
                    MemeMakerRequest(
                        URLImageData(imageReference),
                        line1,
                        line2
                    )
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result.inputStream(),
                        "meme_maker.png"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val input = args.joinToString(" ").split(" | ")
            val line1 = input.getOrNull(0) ?: ""
            val line2 = input.getOrNull(1) ?: ""

            return mapOf(
                options.imageReference to ImageReferenceOrAttachment(
                    args.getOrNull(3),
                    context.getImage(0)
                ),
                options.line1 to line1,
                options.line2 to line2
            )
        }
    }
}