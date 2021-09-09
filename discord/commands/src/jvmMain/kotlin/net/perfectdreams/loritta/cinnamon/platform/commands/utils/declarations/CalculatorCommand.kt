package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object CalculatorCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Calc

    override fun declaration() = command(listOf("calc", "calculadora", "calculator", "calcular", "calculate"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = CalculatorExecutor
    }
}