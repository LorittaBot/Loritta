package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.images.ArtExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object ArtCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Art

    override fun declaration() = command(listOf("art", "arte"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = ArtExecutor
    }
}