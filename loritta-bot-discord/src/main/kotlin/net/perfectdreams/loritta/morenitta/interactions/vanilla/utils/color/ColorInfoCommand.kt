package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.color

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.awt.Color
import java.util.UUID

class ColorInfoCommand(val loritta: LorittaBot): SlashCommandDeclarationWrapper {
    val handler = ColorInfoCommandHandler(loritta)

    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Description,
            category = CommandCategory.UTILS,
            uniqueId = UUID.fromString("e5c4ecaa-77b6-4b01-bc75-ee068442b124")
        ) {
            subcommand(
                name = RgbExecutor.I18N_PREFIX.Label,
                description = RgbExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("8146f88f-a05a-4a6e-99aa-73131bd2853e")
            ) {
                executor = RgbExecutor(handler)
            }
            subcommand(
                name = HexExecutor.I18N_PREFIX.Label,
                description = HexExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("1133344b-4f69-4480-bf68-93368c6c7915")
            ) {
                executor = HexExecutor(handler)
            }
            subcommand(
                name = DecimalExecutor.I18N_PREFIX.Label,
                description = DecimalExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("6f96d57b-bfec-4af4-bfa6-f21e4c73b065")
            ) {
                executor = DecimalExecutor(handler)
            }
        }

    class RgbExecutor(val handler: ColorInfoCommandHandler): LorittaSlashCommandExecutor() {
        class Options : ApplicationCommandOptions() {
            // TODO: replace with integers
            val red = long(
                "red",
                I18N_PREFIX.Options.Red.Text
            )
            val green = long(
                "green",
                I18N_PREFIX.Options.Green.Text
            )
            val blue = long(
                "blue",
                I18N_PREFIX.Options.Blue.Text
            )
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val red = args[options.red].toInt()
            val green = args[options.green].toInt()
            val blue = args[options.blue].toInt()

            if (red !in 0..255 || green !in 0..255 || blue !in 0..255)
                context.fail(ephemeral = true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidColor),
                        Emotes.Error
                    )
                }

            handler.execute(
                context,
                Color(red, green, blue)
            )
        }

        companion object {
            val I18N_PREFIX = ColorInfoCommand.I18N_PREFIX.RgbColorInfo
        }
    }

    class HexExecutor(val handler: ColorInfoCommandHandler): LorittaSlashCommandExecutor() {
        class Options : ApplicationCommandOptions() {
            val hex = string(
                "hex",
                I18N_PREFIX.Options.Hex.Text
            )
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val hex = args[options.hex]

            val hexMatcher = HEX_PATTERN.matcher(hex)

            if (!hexMatcher.find())
                context.fail(ephemeral = true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidColor),
                        Emotes.Error
                    )
                }

            handler.execute(context, Color.decode("#" + hexMatcher.group(1)))
        }

        companion object {
            val I18N_PREFIX = ColorInfoCommand.I18N_PREFIX.HexColorInfo
            val HEX_PATTERN = "#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})".toPattern()
        }
    }

    class DecimalExecutor(val handler: ColorInfoCommandHandler): LorittaSlashCommandExecutor() {
        class Options : ApplicationCommandOptions() {
            val decimal = long(
                "decimal",
                I18N_PREFIX.Options.Decimal.Text
            )
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val decimal = args[options.decimal].toInt()

            val color = Color(decimal)
            handler.execute(context, color)
        }

        companion object {
            val I18N_PREFIX = ColorInfoCommand.I18N_PREFIX.DecimalColorInfo
        }
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Colorinfo
    }
}