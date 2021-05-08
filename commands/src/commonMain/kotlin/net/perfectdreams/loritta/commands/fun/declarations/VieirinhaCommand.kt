package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.VieirinhaExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object VieirinhaCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.vieirinha"

    override fun declaration() = command(listOf("vieirinha", "8ball", "magicball", "magic8ball"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = VieirinhaExecutor
    }
}