package net.perfectdreams.loritta.commands.vanilla.utils.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.commands.vanilla.utils.CalculatorCommand
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object CalculatorCommandDeclaration : CommandDeclaration(
    name = "calc",
    description = LocaleKeyData("${CalculatorCommand.LOCALE_PREFIX}.description")
) {
    override val options = Options

    object Options : CommandDeclaration.Options() {
        // TODO: Fix locale
        val expression = string("expression", LocaleKeyData("idk"))
            .required()
            .register()
    }
}