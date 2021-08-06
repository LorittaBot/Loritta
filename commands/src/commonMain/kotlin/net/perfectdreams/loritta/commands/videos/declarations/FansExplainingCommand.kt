package net.perfectdreams.loritta.commands.videos.declarations

import net.perfectdreams.loritta.commands.videos.FansExplainingExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object FansExplainingCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.fansexplaining"

    override fun declaration() = command(listOf("fansexplaining"), CommandCategory.VIDEOS, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = FansExplainingExecutor
    }
}