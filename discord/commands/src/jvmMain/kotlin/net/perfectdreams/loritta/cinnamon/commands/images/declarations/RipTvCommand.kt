package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.RipTvExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object RipTvCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Riptv

    override fun declaration() = command(listOf("riptv"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = RipTvExecutor
    }
}