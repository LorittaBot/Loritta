package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object BolsonaroCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.bolsonaro"

    override fun declaration() = command(listOf("bolsonaro", "bolsonarotv")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = BolsonaroExecutor
    }
}