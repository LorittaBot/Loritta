package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object TerminatorAnimeCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.terminator"

    override fun declaration() = command(listOf("terminatoranime", "terminator", "animeterminator"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = TerminatorAnimeExecutor
    }
}