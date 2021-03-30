package net.perfectdreams.loritta.commands.vanilla.`fun`.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.commands.vanilla.`fun`.RateWaifuCommand
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object RateWaifuCommandDeclaration : CommandDeclaration(
    name = "ratewaifu",
    description = LocaleKeyData("${RateWaifuCommand.LOCALE_PREFIX}.description")
) {
    override val options = Options

    object Options : CommandDeclaration.Options() {
        // TODO: Fix locale
        val waifu = string("waifu", LocaleKeyData("idk"))
            .required()
            .register()
    }
}