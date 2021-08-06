package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.RipTvExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object RipTvCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.riptv"

    override fun declaration() = command(listOf("riptv"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = RipTvExecutor
    }
}