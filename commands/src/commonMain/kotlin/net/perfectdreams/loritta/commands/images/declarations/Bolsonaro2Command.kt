package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object Bolsonaro2Command : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.bolsonaro"

    override fun declaration() = command(listOf("bolsonaro2", "bolsonarotv2")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = Bolsonaro2Executor
    }
}