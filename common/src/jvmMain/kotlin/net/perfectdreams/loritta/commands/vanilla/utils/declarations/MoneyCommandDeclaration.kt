package net.perfectdreams.loritta.commands.vanilla.utils.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.choice
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.commands.vanilla.utils.MoneyCommand
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object MoneyCommandDeclaration : CommandDeclaration(
    name = "money",
    description = LocaleKeyData("${MoneyCommand.LOCALE_PREFIX}.description")
) {
    override val options = Options

    object Options : CommandDeclaration.Options() {
        // TODO: Fix locale
        val from = string("from", LocaleKeyData("idk"))
            .let {
                var option = it

                MoneyCommand.AVAILABLE_CURRENCIES.forEach { currencyId ->
                    option = option.choice(currencyId, currencyId)
                }

                option
            }
            .required()
            .register()

        val to = string("to", LocaleKeyData("idk"))
            .let {
                var option = it

                MoneyCommand.AVAILABLE_CURRENCIES.forEach { currencyId ->
                    option = option.choice(currencyId, currencyId)
                }

                option
            }
            .required()
            .register()

        val quantity = string("quantity", LocaleKeyData("idk"))
            .register()
    }
}