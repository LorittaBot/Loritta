package net.perfectdreams.loritta.commands.vanilla.`fun`.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.commands.vanilla.`fun`.CoinFlipCommand
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object CoinFlipCommandDeclaration : CommandDeclaration(
    name = "coinflip",
    description = LocaleKeyData("${CoinFlipCommand.LOCALE_PREFIX}.description")
)