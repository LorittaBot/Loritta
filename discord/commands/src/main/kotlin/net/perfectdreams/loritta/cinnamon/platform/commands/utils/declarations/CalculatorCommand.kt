package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.CalculatorExecutor

object CalculatorCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Calc

    override fun declaration() = slashCommand(listOf("calc", "calculadora", "calculator", "calcular", "calculate"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = CalculatorExecutor
    }
}