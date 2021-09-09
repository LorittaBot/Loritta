package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.images.SustoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object SustoCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Fright

    override fun declaration() = command(listOf("scared", "fright", "susto"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = SustoExecutor
    }
}