package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object GetOverHereCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.getoverhere"

    override fun declaration() = command(listOf("getoverhere"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = GetOverHereExecutor
    }
}