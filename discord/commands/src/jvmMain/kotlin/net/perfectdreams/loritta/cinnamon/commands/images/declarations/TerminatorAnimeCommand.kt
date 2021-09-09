package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object TerminatorAnimeCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Terminatoranime

    override fun declaration() = command(listOf("terminatoranime", "terminator", "animeterminator"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = TerminatorAnimeExecutor
    }
}