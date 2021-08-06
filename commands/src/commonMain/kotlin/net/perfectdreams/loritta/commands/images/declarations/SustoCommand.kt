package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.SustoExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object SustoCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.susto"

    override fun declaration() = command(listOf("scared", "fright", "susto"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = SustoExecutor
    }
}