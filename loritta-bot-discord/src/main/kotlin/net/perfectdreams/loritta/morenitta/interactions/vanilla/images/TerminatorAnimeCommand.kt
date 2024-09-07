package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.TerminatorAnimeRequest
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class TerminatorAnimeCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Terminatoranime
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES, UUID.fromString("d29e7262-9626-4802-89d0-c8e3da6abdf4")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("terminatoranime")
            add("terminator")
        }

        executor = TerminatorAnimeExecutor()
    }

    inner class TerminatorAnimeExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val line1 = string("terminator", I18N_PREFIX.Options.TextTerminator)
            val line2 = string("girl", I18N_PREFIX.Options.TextGirl)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val line1 = args[options.line1]
            val line2 = args[options.line2]

            val result = client.handleExceptions(context) {
                client.images.terminatorAnime(
                    TerminatorAnimeRequest(line1, line2)
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(result, "terminator_anime.png")
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
                options.line1 to line1,
                options.line2 to line2
            )
        }
    }
}