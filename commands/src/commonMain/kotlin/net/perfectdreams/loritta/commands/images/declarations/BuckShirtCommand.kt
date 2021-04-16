package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object BuckShirtCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.buckshirt"

    override fun declaration() = command(listOf("buckshirt", "buckcamisa")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = BuckShirtExecutor
    }
}