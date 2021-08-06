package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object CalculatorCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.calc"

    override fun declaration() = command(listOf("calc", "calculadora", "calculator", "calcular", "calculate"), CommandCategory.UTILS, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = CalculatorExecutor
    }
}