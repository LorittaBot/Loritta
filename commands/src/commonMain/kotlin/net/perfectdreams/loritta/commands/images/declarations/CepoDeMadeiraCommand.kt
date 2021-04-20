package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object CepoDeMadeiraCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.cepo"

    override fun declaration() = command(listOf("cepo"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = CepoDeMadeiraExecutor
    }
}