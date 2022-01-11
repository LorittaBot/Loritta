package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.GiveawayEndExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.GiveawayRerollExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.GiveawaySetupExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration

object GiveawayCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Giveaway

    override fun declaration() = command(listOf("giveaway"), CommandCategory.FUN, TodoFixThisData) {
        subcommand(listOf("setup"), I18N_PREFIX.Setup.Description) {
            executor = GiveawaySetupExecutor
        }

        subcommand(listOf("reroll"), I18N_PREFIX.Reroll.Description) {
            executor = GiveawayRerollExecutor
        }

        subcommand(listOf("end"), I18N_PREFIX.End.Description) {
            executor = GiveawayEndExecutor
        }
    }
}