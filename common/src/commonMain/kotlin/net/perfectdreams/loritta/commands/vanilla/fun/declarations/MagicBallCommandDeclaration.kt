package net.perfectdreams.loritta.commands.vanilla.`fun`.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.commands.vanilla.`fun`.MagicBallCommand
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object MagicBallCommandDeclaration : CommandDeclaration(
    name = "vieirinha",
    description = LocaleKeyData("${MagicBallCommand.LOCALE_PREFIX}.description")
) {
    override val options = Options

    object Options : CommandDeclaration.Options() {
        // TODO: Fix locale
        val question = string("question", LocaleKeyData("idk"))
            .required()
            .register()
    }
}