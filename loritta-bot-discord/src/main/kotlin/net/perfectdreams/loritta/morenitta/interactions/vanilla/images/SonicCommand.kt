package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.ManiaTitleCardRequest
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerSingleCommandBase

class SonicCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Sonic
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.IMAGES) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Knuxthrow.Label, I18N_PREFIX.Knuxthrow.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("knuxthrow")
                add("knucklesthrow")
                add("throwknux")
                add("throwknuckles")
                add("knucklesjogar")
                add("knuxjogar")
                add("jogarknuckles")
                add("jogarknux")
            }

            executor = KnuxThrowExecutor()
        }

        subcommand(I18N_PREFIX.Maniatitlecard.Label, I18N_PREFIX.Maniatitlecard.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("maniatitlecard")
            }
            executor = ManiaTitleCardExecutor()
        }

        subcommand(I18N_PREFIX.Studiopolistv.Label, I18N_PREFIX.Studiopolistv.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("studiopolistv")
                add("studiopolis")
            }

            executor = StudiopolisTvExecutor()
        }
    }

    inner class KnuxThrowExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.knucklesThrow(it) },
        "knux_throw.gif"
    )

    inner class ManiaTitleCardExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val line1 = string("line1", I18N_PREFIX.Maniatitlecard.Options.Line1)
            val line2 = optionalString("line2", I18N_PREFIX.Maniatitlecard.Options.Line2)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val line1 = args[options.line1]
            val line2 = args[options.line2]

            val result = client.handleExceptions(context) {
                client.images.maniaTitleCard(
                    ManiaTitleCardRequest(
                        line1,
                        line2
                    )
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(result, "mania_title_card.png")
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

    inner class StudiopolisTvExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.studiopolisTv(it) },
        "studiopolis_tv.png"
    )
}