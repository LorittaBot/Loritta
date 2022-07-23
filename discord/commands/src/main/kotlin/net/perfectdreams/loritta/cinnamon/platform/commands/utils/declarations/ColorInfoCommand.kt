package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo.DecimalColorInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo.HexColorInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo.RgbColorInfoExecutor

object ColorInfoCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Colorinfo

    override fun declaration() = slashCommand(listOf("colorinfo"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand(listOf("rgb"), I18N_PREFIX.RgbColorInfo.Description) {
            executor = RgbColorInfoExecutor
        }

        subcommand(listOf("hex"), I18N_PREFIX.HexColorInfo.Description) {
            executor = HexColorInfoExecutor
        }

        subcommand(listOf("decimal"), I18N_PREFIX.DecimalColorInfo.Description) {
            executor = DecimalColorInfoExecutor
        }
    }
}