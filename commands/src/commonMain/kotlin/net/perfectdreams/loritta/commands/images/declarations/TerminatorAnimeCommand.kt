package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object TerminatorAnimeCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Terminatoranime

    override fun declaration() = command(listOf("terminatoranime", "terminator", "animeterminator"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = TerminatorAnimeExecutor
    }
}