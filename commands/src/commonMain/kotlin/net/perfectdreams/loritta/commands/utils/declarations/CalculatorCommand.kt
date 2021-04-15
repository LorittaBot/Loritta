package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object CalculatorCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.calc"

    override fun declaration() = command(listOf("calc", "calculadora", "calculator", "calcular", "calculate")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = CalculatorExecutor
    }
}