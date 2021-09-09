package net.perfectdreams.loritta.cinnamon.commands.videos.declarations

import net.perfectdreams.loritta.cinnamon.commands.videos.FansExplainingExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object FansExplainingCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Fansexplaining

    override fun declaration() = command(listOf("fansexplaining"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = FansExplainingExecutor
    }
}