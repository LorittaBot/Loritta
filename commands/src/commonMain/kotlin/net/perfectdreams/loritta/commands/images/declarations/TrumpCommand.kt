package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.TrumpExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object TrumpCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Trump

    override fun declaration() = command(listOf("trump"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = TrumpExecutor
    }
}