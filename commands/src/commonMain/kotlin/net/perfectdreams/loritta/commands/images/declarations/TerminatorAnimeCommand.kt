package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object TerminatorAnimeCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.terminator"

    override fun declaration() = command(listOf("terminatoranime", "terminator", "animeterminator"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = TerminatorAnimeExecutor
    }
}