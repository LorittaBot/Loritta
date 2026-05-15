package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.color

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.ColorUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.util.UUID

class ColorInfoCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Colorinfo
    }

    val handler = ColorInfoCommandHandler(loritta)

    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Description,
            category = CommandCategory.UTILS,
            uniqueId = UUID.fromString("e5c4ecaa-77b6-4b01-bc75-ee068442b124")
        ) {
            enableLegacyMessageSupport = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("rgb")
                add("hex")
                add("hexcolor")
                add("colorpick")
                add("colorpicker")
            }

            examples = I18N_PREFIX.Examples

            executor = ColorInfoExecutor(handler)
        }

    class ColorInfoExecutor(val handler: ColorInfoCommandHandler) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val color = string("color", I18N_PREFIX.Options.Color.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val input = args[options.color]
            val color = ColorUtils.getColorFromString(input)

            if (color == null) {
                context.fail(ephemeral = true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidColor),
                        Emotes.Error
                    )
                }
            }

            handler.execute(context, color)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(options.color to args.joinToString(" "))
        }
    }
}
