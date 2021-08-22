package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.MemeMakerExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object MemeMakerCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Mememaker

    override fun declaration() = command(listOf("mememaker"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = MemeMakerExecutor
    }
}