package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.QuadroExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object QuadroCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.wolverine"

    override fun declaration() = command(listOf("quadro", "frame", "picture", "wolverine"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = QuadroExecutor
    }
}