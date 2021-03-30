package net.perfectdreams.loritta.commands.vanilla.utils.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.commands.vanilla.utils.AnagramCommand
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object AnagramCommandDeclaration : CommandDeclaration(
    name = "anagram",
    description = LocaleKeyData("${AnagramCommand.LOCALE_PREFIX}.description")
) {
    override val options = Options

    object Options : CommandDeclaration.Options() {
        // TODO: Fix locale
        val text = string("text", LocaleKeyData("idk"))
            .required()
            .register()
    }
}