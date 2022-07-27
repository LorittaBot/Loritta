package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.CalculatorExecutor

class CalculatorCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Calc
    }

    override fun declaration() = slashCommand("calc", CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = CalculatorExecutor(loritta)
    }
}