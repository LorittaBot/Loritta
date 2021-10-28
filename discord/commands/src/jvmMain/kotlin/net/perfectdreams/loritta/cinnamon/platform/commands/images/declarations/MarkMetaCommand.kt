package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MarkMetaExecutor

object MarkMetaCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Markmeta

    override fun declaration() = command(listOf("markmeta"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = MarkMetaExecutor
    }
}