package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object CalculatorCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Calc

    override fun declaration() = command(listOf("calc", "calculadora", "calculator", "calcular", "calculate"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = CalculatorExecutor
    }
}