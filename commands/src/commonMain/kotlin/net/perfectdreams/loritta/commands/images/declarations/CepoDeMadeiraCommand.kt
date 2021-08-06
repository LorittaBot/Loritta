package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object CepoDeMadeiraCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.cepo"

    override fun declaration() = command(listOf("cepo"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = CepoDeMadeiraExecutor
    }
}